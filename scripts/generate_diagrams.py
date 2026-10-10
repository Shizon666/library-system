# -*- coding: utf-8 -*-
"""生成论文用技术架构图（与当前代码一致）"""
import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch
import matplotlib.lines as mlines

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False

OUT_DIR = os.path.join(os.path.dirname(__file__), '..', 'docx_diagrams')
os.makedirs(OUT_DIR, exist_ok=True)


def save(fig, name):
    path = os.path.join(OUT_DIR, name)
    fig.savefig(path, dpi=150, bbox_inches='tight', facecolor='white', pad_inches=0.3)
    plt.close(fig)
    print('saved', path)
    return path


def draw_box(ax, x, y, w, h, text, fc='#E8F0FE', ec='#1E3A8A', fs=9):
    box = FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.02,rounding_size=0.05",
                         facecolor=fc, edgecolor=ec, linewidth=1.2)
    ax.add_patch(box)
    ax.text(x + w/2, y + h/2, text, ha='center', va='center', fontsize=fs, wrap=True)


def draw_entity(ax, x, y, name, attrs, w=2.2, h=None):
    lines = [name] + attrs
    h = h or (0.35 + 0.28 * len(lines))
    box = FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.01,rounding_size=0.03",
                         facecolor='#FFF7ED', edgecolor='#C2410C', linewidth=1.5)
    ax.add_patch(box)
    ax.text(x + w/2, y + h - 0.22, name, ha='center', va='center', fontsize=10, fontweight='bold')
    ax.plot([x, x + w], [y + h - 0.38, y + h - 0.38], color='#C2410C', lw=1)
    for i, a in enumerate(attrs):
        ax.text(x + 0.08, y + h - 0.55 - i * 0.26, a, ha='left', va='center', fontsize=8)
    return x + w/2, y + h/2


def gen_er_diagram():
    fig, ax = plt.subplots(figsize=(14, 9))
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 9)
    ax.axis('off')
    ax.set_title('图4-1  图书馆管理系统 E-R 图', fontsize=14, fontweight='bold', pad=12)

    entities = {
        'student': (0.5, 6.2, ['sno(PK)', 'username', 'type', 'college', 'major', 'graduated']),
        'libcard': (3.5, 6.2, ['cardNo(PK)', 'sno(FK)', 'cardStatus', 'times', 'sname...']),
        'book': (7.0, 6.2, ['id(PK)', 'ISBN', 'bname', 'author', 'bookStatus', 'price']),
        'bookcopy': (10.0, 6.2, ['barCode(PK)', 'ISBN(FK)', 'place', 'status', 'oldStatus']),
        'borrowrec': (1.5, 2.8, ['serNum(PK)', 'sno(FK)', 'barCode(FK)', 'retStatus', 'fineMoney', 'fineStatus', 'paySerNum(FK)']),
        'payrec': (5.5, 2.8, ['serNum(PK)', 'sno(FK)', 'payAmount', 'payDate']),
        'cardrec': (9.0, 2.8, ['serNum(PK)', 'sno(FK)', 'originCardNo', 'newCardNo', 'opType']),
        'admin': (0.5, 0.5, ['id(PK)', 'username', 'password', 'adminType']),
        'notice': (4.0, 0.5, ['id(PK)', 'topic', 'content', 'author', 'createDate']),
    }
    centers = {}
    for key, (x, y, attrs) in entities.items():
        cx, cy = draw_entity(ax, x, y, key, attrs)
        centers[key] = (cx, cy)

    def rel(a, b, label, offset=(0, 0)):
        ax.annotate('', xy=(centers[b][0] + offset[0], centers[b][1] + offset[1]),
                    xytext=(centers[a][0], centers[a][1]),
                    arrowprops=dict(arrowstyle='-', color='#64748B', lw=1.2))
        mx = (centers[a][0] + centers[b][0]) / 2 + offset[0]
        my = (centers[a][1] + centers[b][1]) / 2 + offset[1]
        ax.text(mx, my, label, fontsize=8, color='#334155', ha='center',
                bbox=dict(boxstyle='round,pad=0.2', fc='white', ec='#CBD5E1', alpha=0.9))

    rel('student', 'libcard', '1:N')
    rel('student', 'borrowrec', '1:N')
    rel('book', 'bookcopy', '1:N')
    rel('bookcopy', 'borrowrec', '1:N')
    rel('payrec', 'borrowrec', '1:N\n(paySerNum)')
    rel('student', 'cardrec', '1:N')
    rel('student', 'payrec', '1:N')

    ax.text(7, 0.2, '注：罚款信息内嵌于 borrowrec（fineMoney/fineStatus），无独立 fine/overdue 表',
            ha='center', fontsize=8, color='#64748B', style='italic')
    save(fig, 'fig4-1_er.png')


def gen_bs_architecture():
    fig, ax = plt.subplots(figsize=(10, 7))
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 7)
    ax.axis('off')
    ax.set_title('图5-1  B/S 四层架构图', fontsize=14, fontweight='bold', pad=12)

    layers = [
        ('表现层（Browser + Vue 3 SPA）', 'views/ · layouts/ · components/\nElement Plus · Pinia · Vue Router(Hash)', '#DBEAFE'),
        ('控制层（Spring Boot Controller）', 'Auth · Book · Borrow · Card · Fine\nDashboard · Me · Portal（8 个 Controller）', '#D1FAE5'),
        ('业务层（Service + @Transactional）', 'AuthService · BookService · BorrowService\nCardService · FineService · DashboardService · MeService', '#FEF3C7'),
        ('数据层（MyBatis Mapper + MySQL）', 'Entity + Mapper XML → library 库（9 张业务表）', '#FCE7F3'),
    ]
    y = 5.5
    for title, detail, color in layers:
        draw_box(ax, 1, y, 8, 1.0, f'{title}\n{detail}', fc=color, fs=9)
        y -= 1.3

    ax.annotate('', xy=(5, 1.2), xytext=(5, 5.5),
                arrowprops=dict(arrowstyle='<->', color='#1E3A8A', lw=2))
    ax.text(9.2, 3.5, 'Axios\nREST\n/api', fontsize=9, color='#1E3A8A', ha='center')
    ax.text(5, 0.5, 'Vite Dev Proxy: /api → localhost:8080  |  统一响应 { code, message, data }',
            ha='center', fontsize=8, color='#64748B')
    save(fig, 'fig5-1_bs.png')


def gen_module_diagram():
    fig, ax = plt.subplots(figsize=(12, 8))
    ax.set_xlim(0, 12)
    ax.set_ylim(0, 8)
    ax.axis('off')
    ax.set_title('图5-2  系统功能模块划分图', fontsize=14, fontweight='bold', pad=12)

    draw_box(ax, 4.5, 6.8, 3, 0.7, '图书管理系统', fc='#1E3A8A', ec='#1E3A8A', fs=11)
    ax.text(6, 6.8, '图书管理系统', ha='center', va='center', fontsize=11, color='white', fontweight='bold')

    modules = [
        (0.3, 4.8, '公共门户\nHomePortalView\n借书/还书工位'),
        (2.5, 4.8, '认证模块\nLoginView\n双角色登录'),
        (4.7, 4.8, '图书检索\nBookSearchView\n详情抽屉'),
        (6.9, 4.8, '个人中心\nPersonalView\n在借/历史/罚款'),
        (9.1, 4.8, '罚款管理\nFineManageView\n合并缴纳'),
        (1.0, 2.5, '证件管理\nCardManageView\n新办/挂失/补办/注销'),
        (3.8, 2.5, '采编管理\nCatalog/Form/Copies\n入库/下架/报废'),
        (6.6, 2.5, '流通管理\nBorrowView/ReturnView\n批量借还/挂失赔偿'),
        (9.4, 2.5, '统计看板\nDashboardView\nKPI + 排行榜'),
    ]
    for x, y, text in modules:
        draw_box(ax, x, y, 2.0, 1.5, text, fs=8)
        ax.plot([x + 1.0, 6], [y + 1.5, 6.8], color='#94A3B8', lw=0.8, ls='--')

    ax.text(6, 0.8, '管理员：dashboard / cards / books/*  |  学生：home / personal / books/search  |  公开：home / borrow / return',
            ha='center', fontsize=8, color='#64748B')
    save(fig, 'fig5-2_modules.png')


def gen_mapper_uml():
    fig, ax = plt.subplots(figsize=(11, 7))
    ax.set_xlim(0, 11)
    ax.set_ylim(0, 7)
    ax.axis('off')
    ax.set_title('图5-3  Mapper 层 UML 类图', fontsize=14, fontweight='bold', pad=12)

    mappers = [
        ('AdminMapper', 'findByUsername'),
        ('StudentMapper', 'findBySno · updatePassword'),
        ('LibcardMapper', 'findByCardNo · insert · updateStatus'),
        ('BookMapper', 'search · findByIsbn · insert · update'),
        ('BookcopyMapper', 'findByBarCode · insert · updateStatus'),
        ('BorrowrecMapper', 'insert · countActive · updateReturn'),
        ('CardrecMapper', 'insert · listBySno · listAll'),
        ('FineMapper', 'listFines · sumUnpaid · updatePaid'),
        ('PayrecMapper', 'insert'),
        ('DashboardMapper', 'getStats · getRankings'),
    ]
    cols = 2
    for i, (name, methods) in enumerate(mappers):
        col, row = i % cols, i // cols
        x, y = 0.5 + col * 5.2, 5.2 - row * 1.5
        draw_box(ax, x, y, 4.8, 1.2, f'<<interface>>\n{name}\n─────────\n{methods}', fs=8)

    ax.text(5.5, 0.3, 'MyBatis 动态代理 + resources/mapper/*.xml 实现 SQL 映射',
            ha='center', fontsize=9, color='#64748B')
    save(fig, 'fig5-3_mapper.png')


def gen_controller_service_uml():
    fig, ax = plt.subplots(figsize=(12, 8))
    ax.set_xlim(0, 12)
    ax.set_ylim(0, 8)
    ax.axis('off')
    ax.set_title('图5-4  Controller 与 Service 层 UML 类图', fontsize=14, fontweight='bold', pad=12)

    pairs = [
        ('AuthController', 'AuthService', 'login()'),
        ('BookController', 'BookService', 'search · acquire · offShelf · discardCopy'),
        ('BorrowController', 'BorrowService', 'borrow · return · reportLoss'),
        ('CardController', 'CardService', 'create · loss · replace · delete'),
        ('FineController', 'FineService', 'list · pay'),
        ('DashboardController', 'DashboardService', 'stats · rankings'),
        ('MeController', 'MeService', 'profile · password · borrowings'),
        ('PortalController', 'DashboardService', 'home()'),
    ]
    for i, (ctrl, svc, methods) in enumerate(pairs):
        y = 6.8 - i * 0.85
        draw_box(ax, 0.5, y, 3.5, 0.65, ctrl, fc='#DBEAFE', fs=8)
        draw_box(ax, 5.0, y, 3.5, 0.65, svc, fc='#D1FAE5', fs=8)
        draw_box(ax, 9.0, y, 2.5, 0.65, methods, fc='#F8FAFC', ec='#94A3B8', fs=7)
        ax.annotate('', xy=(5.0, y + 0.32), xytext=(4.0, y + 0.32),
                    arrowprops=dict(arrowstyle='->', color='#1E3A8A', lw=1))

    draw_box(ax, 0.5, 0.3, 11, 0.55, '公共组件：GlobalExceptionHandler · Result · BusinessException · LibraryProperties · CorsConfig',
            fc='#FEF3C7', fs=8)
    save(fig, 'fig5-4_controller_service.png')


def gen_borrow_sequence():
    fig, ax = plt.subplots(figsize=(13, 8))
    ax.set_xlim(0, 13)
    ax.set_ylim(0, 8)
    ax.axis('off')
    ax.set_title('图5-5  借书业务时序图', fontsize=14, fontweight='bold', pad=12)

    actors = ['BorrowView', 'BorrowController', 'BorrowService', 'CardService', 'Mapper/DB']
    xs = [1.2, 3.5, 6.0, 8.5, 11.0]
    for x, name in zip(xs, actors):
        draw_box(ax, x - 0.7, 7.0, 1.4, 0.5, name, fs=8)
        ax.plot([x, x], [0.5, 7.0], color='#CBD5E1', lw=1, ls='--')

    steps = [
        (0, 1, 6.3, '1. GET /cards/{cardNo}/borrow-check'),
        (1, 2, 5.8, '2. POST /api/borrow {cardNo, barCodes[]}'),
        (2, 2, 5.3, '3. borrow(cardNo, barCodes)'),
        (2, 3, 4.8, '4. 校验证件状态(正常)'),
        (2, 4, 4.3, '5. 校验未缴罚款 ≤ 50元'),
        (2, 4, 3.8, '6. 校验在借数 ≤ 上限(5/10)'),
        (2, 4, 3.3, '7. 校验副本可借 & 书目上架'),
        (2, 4, 2.8, '8. INSERT borrowrec, UPDATE bookcopy.status=0'),
        (2, 4, 2.3, '9. UPDATE libcard.times+1'),
        (2, 1, 1.8, '10. 返回应还日期列表'),
        (1, 0, 1.3, '11. 展示借书成功提示'),
    ]
    for src, dst, y, label in steps:
        sx, dx = xs[src], xs[dst]
        style = '->' if src != dst else '->'
        if src == dst:
            ax.annotate('', xy=(sx + 0.3, y), xytext=(sx, y),
                        arrowprops=dict(arrowstyle='->', color='#1E3A8A', lw=1))
        else:
            ax.annotate('', xy=(dx, y), xytext=(sx, y),
                        arrowprops=dict(arrowstyle='->', color='#1E3A8A', lw=1))
        ax.text((sx + dx) / 2, y + 0.08, label, fontsize=7, ha='center', color='#334155')

    ax.text(6.5, 0.5, '@Transactional：任一副本校验失败则整体回滚',
            ha='center', fontsize=8, color='#64748B', style='italic')
    save(fig, 'fig5-5_sequence.png')


def gen_frontend_structure():
    fig, ax = plt.subplots(figsize=(11, 6))
    ax.set_xlim(0, 11)
    ax.set_ylim(0, 6)
    ax.axis('off')
    ax.set_title('图5-6  前端模块结构图', fontsize=14, fontweight='bold', pad=12)

    draw_box(ax, 4, 5.0, 3, 0.6, 'src/', fc='#1E3A8A', ec='#1E3A8A', fs=10)
    ax.text(5.5, 5.3, 'src/', ha='center', va='center', color='white', fontweight='bold')

    branches = [
        (0.3, 3.5, 'router/\nindex.js\n导航守卫'),
        (2.3, 3.5, 'stores/\nauth.js\n登录态'),
        (4.3, 3.5, 'api/\nrequest.js\n8个模块'),
        (6.3, 3.5, 'layouts/\nMainLayout\nSidebar+Topbar'),
        (8.3, 3.5, 'components/\nStatusBadge\nPagination'),
        (1.0, 1.5, 'views/login\nviews/portal\nviews/borrow\nviews/return'),
        (4.0, 1.5, 'views/dashboard\nviews/card\nviews/book/*'),
        (7.0, 1.5, 'views/fine\nviews/personal'),
    ]
    for x, y, text in branches:
        draw_box(ax, x, y, 2.0, 1.3, text, fs=8)
        ax.plot([x + 1.0, 5.5], [y + 1.3, 5.0], color='#94A3B8', lw=0.8, ls='--')
    save(fig, 'fig5-6_frontend.png')


def gen_ui_mock(name, title, sections):
    """生成 UI 线框示意图（无运行环境时使用）"""
    fig, ax = plt.subplots(figsize=(10, 6.5))
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 6.5)
    ax.axis('off')

    # sidebar
    sidebar = FancyBboxPatch((0, 0), 1.6, 6.5, boxstyle="square,pad=0",
                             facecolor='#1E293B', edgecolor='none')
    ax.add_patch(sidebar)
    ax.text(0.8, 6.1, 'Sidebar', ha='center', color='white', fontsize=8)
    for i, item in enumerate(['首页', '借书', '还书', '检索', '...']):
        ax.text(0.8, 5.5 - i * 0.5, item, ha='center', color='#94A3B8', fontsize=7)

    # topbar
    topbar = FancyBboxPatch((1.6, 5.7), 8.4, 0.8, boxstyle="square,pad=0",
                            facecolor='white', edgecolor='#E5E7EB')
    ax.add_patch(topbar)
    ax.text(5.8, 6.1, '图书管理系统 — Topbar', ha='center', fontsize=9)

    # content
    content = FancyBboxPatch((1.6, 0), 8.4, 5.7, boxstyle="square,pad=0",
                             facecolor='#F3F4F6', edgecolor='#E5E7EB')
    ax.add_patch(content)
    ax.text(5.8, 5.3, title, ha='center', fontsize=11, fontweight='bold', color='#1E3A8A')

    y = 4.6
    for sec in sections:
        draw_box(ax, 2.0, y, 7.2, 0.55, sec, fc='white', ec='#CBD5E1', fs=8)
        y -= 0.75

    save(fig, name)


def gen_all_ui_mocks():
    gen_ui_mock('ui_login.png', 'LoginView — 双 Tab 登录', [
        '[Tab] 管理员登录 | 学生登录（借书证号）',
        '用户名/证号输入框    密码输入框（6-20位）',
        '☑ 记住密码          [ 登 录 ]',
        '测试账号提示：admin / 123456',
    ])
    gen_ui_mock('ui_portal.png', 'HomePortalView — 公共门户首页', [
        '搜索框：输入书名/作者/ISBN → 跳转检索页',
        '🔥 热读推荐 TOP    ⭐ 借书明星 TOP    📚 新书上架',
        '卡片列表展示书名、作者、出版社',
    ])
    gen_ui_mock('ui_dashboard.png', 'DashboardView — 管理员统计看板', [
        'KPI 卡片：馆藏种数 | 当前借出 | 有效借书证 | 未缴罚款',
        '当月借书学生 TOP10          当月热门图书 TOP10',
        '表格：排名 | 学号/ISBN | 姓名/书名 | 借阅次数',
    ])
    gen_ui_mock('ui_book_search.png', 'BookSearchView — 图书检索', [
        '关键词搜索框  [搜索]',
        '表格：ISBN | 书名 | 作者 | 出版社 | 分类号 | [详情]',
        '→ 右侧 Drawer：简介 + 副本列表（条码/位置/状态 Tag）',
    ])
    gen_ui_mock('ui_borrow.png', 'BorrowView — 借书工位（两步流程）', [
        'Step1: 输入证号 → 资格校验（额度/罚款/状态）→ 确认',
        'Step2: 检索副本条码 → 待借列表 → 批量借出',
        '显示：剩余可借数 / 应还日期提示',
    ])
    gen_ui_mock('ui_return.png', 'ReturnView — 还书工位', [
        '输入借书证号 → 加载在借列表',
        '表格：条码 | 书名 | 借出日 | 应还日 | 超期天数（红色高亮）',
        '选择记录 → 确认还书 → 显示罚款金额',
    ])
    gen_ui_mock('ui_card_create.png', 'CardManageView — 新办借书证', [
        '输入学号 → 查询学生信息（StudentCardOpPanel）',
        '自动生成证号预览（学号+001）',
        '确认新办 → 写入 libcard + cardrec',
    ])
    gen_ui_mock('ui_card_loss.png', 'CardManageView — 挂失/补办/注销', [
        'Tab: 新办 | 挂失 | 补办 | 注销',
        '学号查询 → 展示证件状态（StatusBadge）',
        '补办：旧证注销 + 新证继承 times',
    ])
    gen_ui_mock('ui_book_catalog.png', 'BookCatalogView — 书目编目', [
        '搜索：ISBN/书名/作者/出版社/分类号',
        '表格 + 分页 → [新增] [编辑] [副本管理] [下架]',
    ])
    gen_ui_mock('ui_book_form.png', 'BookFormView — 书目表单', [
        'ISBN | 书名 | 作者 | 出版社 | 出版日期 | 分类号 | 简介',
        '新增 → 跳转副本管理；编辑模式 ISBN 不可改',
    ])
    gen_ui_mock('ui_book_copies.png', 'BookCopiesView — 副本管理', [
        '书目信息摘要',
        '副本列表：条码 | 馆藏位置 | 状态 | [编辑位置] [注销]',
        '新增副本：条码 + 位置（A区-01架-01层 格式校验）',
    ])
    gen_ui_mock('ui_fines.png', 'FineManageView — 罚款管理', [
        '筛选：全部/未缴/已缴    证号输入（管理员）',
        '表格：学号 | 书名 | 超期天数 | 罚款金额 | 缴纳状态',
        '多选未缴 → [合并缴纳]',
    ])
    gen_ui_mock('ui_personal.png', 'PersonalView — 学生个人中心', [
        'Tab: 基本信息 | 借阅记录 | 缴纳罚款 | 修改密码',
        '借阅记录：借还日期 | 还书状态 | 罚款金额',
        '超期行高亮 | 批量缴纳未缴罚款',
    ])


if __name__ == '__main__':
    gen_er_diagram()
    gen_bs_architecture()
    gen_module_diagram()
    gen_mapper_uml()
    gen_controller_service_uml()
    gen_borrow_sequence()
    gen_frontend_structure()
    gen_all_ui_mocks()
    print('All diagrams generated.')
