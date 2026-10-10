# -*- coding: utf-8 -*-
"""根据当前代码更新高级java课设论文.docx"""
import os
import shutil
import copy
from docx import Document
from docx.shared import Inches, Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DOC = os.path.join(BASE, '高级java课设论文.docx')
OUT_DOC = os.path.join(BASE, '高级java课设论文_更新版.docx')
OUT_DOC_FINAL = os.path.join(BASE, '高级java课设论文.docx')
BACKUP = os.path.join(BASE, '高级java课设论文_原版备份.docx')
DIAGRAM_DIR = os.path.join(BASE, 'docx_diagrams')


def replace_paragraph_text(p, new_text):
    if not p.runs:
        p.add_run(new_text)
        return
    p.runs[0].text = new_text
    for r in p.runs[1:]:
        r.text = ''


def clear_paragraph(p):
    for r in p.runs:
        r._element.getparent().remove(r._element)


def set_paragraph_image(p, image_path, width=Inches(5.8)):
    clear_paragraph(p)
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run()
    run.add_picture(image_path, width=width)


def update_table(table, rows_data):
    """rows_data: list of list, first row is header if table has header"""
    # Ensure enough rows
    while len(table.rows) < len(rows_data):
        table.add_row()
    for ri, row_data in enumerate(rows_data):
        row = table.rows[ri]
        for ci, val in enumerate(row_data):
            if ci < len(row.cells):
                row.cells[ci].text = val


# ── 段落文本更新映射（index -> new text）──
PARA_UPDATES = {
    10: '时    间          2026.6',
    14: ('本系统面向学校图书馆及小型书屋场景，采用 B/S 架构，通过浏览器即可完成图书检索、借还办理、'
         '证件管理、罚款缴纳与数据统计等核心业务。系统按职能部门划分为办公室（证件管理）、采编部（入库/下架/报废）、'
         '流通部（借还/罚款）及读者四类角色，减轻馆务工作负担，同时为读者提供门户首页与自助查询能力。'),
    17: '实现图书采编入库、副本管理、整书下架与单册报废等馆藏维护功能；',
    21: '实现管理员与读者双角色登录，读者可在个人中心查询在借、历史与罚款记录；',
    22: '实现公共门户首页、管理员统计看板与当月借阅排行榜。',
    29: ('前后端通过 Axios 以 RESTful 风格通信，开发环境下 Vite 将 /api 代理至后端 8080 端口；'
         '接口统一返回 { code, message, data } 格式。学生登录后通过请求头 X-Role、X-Sno、X-Card-No 传递身份信息。'),
    33: ('图书管理：新书采编入库（BookFormView 录入 ISBN、书名、作者、出版社、分类号、简介；'
         'BookCopiesView 管理副本条码与馆藏位置）；按 ISBN 整书下架；按条码注销单个副本；'
         '支持 ISBN 10/13 位校验与馆藏位置格式校验（A区-01架-01层）。'),
    34: ('借书证管理：为新办学生自动生成证号（学号+3位流水，如 2021101001001）；'
         '办理挂失、补办（旧证注销、新证继承在借计数 times）、注销（仅已毕业学生）；'
         '查询证件详情及 cardrec 操作流水，支持全部办证记录筛选。'),
    35: ('流通管理：借书工位（BorrowView）先校验借书资格再批量扫描条码借出；'
         '还书工位（ReturnView）按证号查在借列表并办理还书；支持图书挂失赔偿（按书目定价）。'),
    36: ('罚款管理：查询罚款记录（数据来源于 borrowrec.fineMoney），按缴纳状态筛选，'
         '支持多笔未缴罚款合并缴纳并写入 payrec 流水。'),
    37: ('数据统计：管理员 Dashboard 展示馆藏种数、当前借出册数、有效借书证数、未缴罚款笔数；'
         '展示当月借书学生 TOP10 与热门图书 TOP10。公共门户 HomePortalView 展示热读推荐、借书明星与新书上架。'),
    39: '账号登录：学生使用借书证号与密码登录（证件须为「正常」状态，挂失/注销证不可登录）；支持记住密码。',
    40: '图书检索：按书名/作者/ISBN/出版社关键词模糊搜索（仅上架书目）；Drawer 展示详情及副本条码、馆藏位置与状态。',
    41: ('借还操作：借书/还书工位公开可访问；读者登录后自动填充证号。'
         '借书分两步：资格校验（额度/罚款/证件状态）→ 批量扫描条码；还书按证号加载在借列表，超期行高亮并计罚。'),
    42: '个人中心：四个 Tab — 基本信息、借阅记录（含还书状态与罚款）、缴纳罚款（批量合并）、修改密码。',
    44: '统一登录入口：管理员使用用户名登录，读者使用借书证号登录；路由守卫 + 侧边栏按角色展示不同菜单。',
    46: '证件操作流水：新办/挂失/补办/注销均写入 cardrec 表，可按学号查询或管理员查看全部记录。',
    51: ('借书前需串联多项校验：证件 cardStatus 为「正常」、未缴罚款累计不超过 50 元、'
         '当前在借数量不超过上限（本科生 5 本/研究生 10 本，按 libcard.times 与 borrowrec 统计）、'
         '每本副本 status=1 且书目 bookStatus=1（上架）。规则集中在 BorrowService.borrow()，'
         '使用 @Transactional 保证批量借书原子性。'),
    52: ('还书时判断是否超期：逾期天数 = 实际归还日 − 应还日（最少计 1 天），'
         '罚款 = 逾期天数 × 1 元/天，直接写入 borrowrec.ovdDays、fineMoney、fineStatus（未缴）。'
         '按时还书则 fineMoney=0 且 fineStatus=已缴。'),
    54: ('系统采用「书目 + 副本」两级库存模型：book 表维护品种元数据（bookStatus 控制上架/下架），'
         'bookcopy 表维护每册实体状态（1=可借/0=借出/2=注销）。借书时将副本 status 置 0、'
         'libcard.times 加 1 并写入 borrowrec；还书时恢复副本 status=1、times 减 1。'
         '无书目级冗余计数器，库存以副本状态为准。'),
    55: ('补办业务需同时处理：旧证 cardStatus 置「注销」、新证生成（学号+递增三位流水号）、'
         '继承 times 在借计数、写入 cardrec 流水。挂失期间旧证不可登录也不可借书；'
         '注销仅允许已毕业（student.graduated=1）学生办理。同一学号同时仅一张「正常」证。'),
    56: '',
    58: ('读者可能有多笔未缴超期罚款，系统采用 payrec（缴费主表）+ borrowrec.paySerNum（关联字段）设计：'
         '一次合并缴纳写入一条 payrec，批量更新相关 borrowrec 的 fineStatus 为「已缴」，保证账务可追溯。'
         '缴纳入口分为管理员端 FineManageView 与学生端 PersonalView：前端多选未缴记录后提交 POST /api/fines/pay，'
         '由 FineService.payFines() 在 @Transactional 事务内完成。处理流程为：根据证件号定位学号，逐笔校验罚款有效、'
         '状态为「未缴」且归属于同一学生，累加得 payAmount；向 payrec 插入一条流水（含 sno、payAmount、payDate）；'
         '再调用 BorrowrecMapper.updateFinePaid 批量将所选 borrowrec 的 fineStatus 置「已缴」并回写 paySerNum。'
         '据此既可从缴费记录反查涵盖的借阅明细，学生亦可在个人中心查看历史缴费；未缴罚款累计超过 50 元时借书资格校验将拒绝新借，'
         '与罚款模块形成闭环。'),
    61: ('根据系统业务需求，将实体及属性抽象为 E-R 模型。本系统主要实体包括：'
         '学生、借书证、图书、图书副本、借阅记录、缴费记录、证件操作记录、管理员及公告等。'),
    62: ('实体间主要关系：student 与 libcard 为 1:N（补办场景可持多张历史证）；'
         'book 与 bookcopy 为 1:N；student 与 borrowrec 为 1:N；'
         'bookcopy 与 borrowrec 为 1:N；payrec 与 borrowrec 为 1:N（通过 paySerNum 关联）；'
         'student 与 cardrec 为 1:N。罚款信息内嵌于 borrowrec，无独立 fine/overdue 表。'),
    66: '（1）学生 student：sno、username、type（本科生/研究生）、college、major、birth、originPlace、password、graduated。',
    67: '（2）借书证 libcard：cardNo（PK，学号+3位序号）、sno（FK）、cardStatus（正常/挂失/注销）、times（在借数）及冗余学生信息。',
    68: '（3）图书 book：id、ISBN、bname、author、publisher、introduction、pubDate、clcNum、bookStatus（1上架/0下架）、price。',
    69: '（4）图书副本 bookcopy：barCode（PK）、ISBN（FK）、place、status（1可借/0借出/2注销）、oldStatus。',
    70: '（5）借阅记录 borrowrec：serNum（PK）、sno、barCode、borDate、retDate、realRetDate、retStatus、ovdDays、fineMoney、fineStatus、paySerNum。',
    77: '本系统类主要分为 Mapper 层、Service 层、Controller 层、Entity 实体类、DTO/VO 传输对象五部分，采用 Spring Boot 3.2.5 + MyBatis 3.0.3 分层架构，共 8 个 Controller、7 个 Service、10 个 Mapper。',
    84: 'Mapper 层采用 MyBatis 接口 + XML 映射，由 Spring 动态代理实现，包括 AdminMapper、StudentMapper、LibcardMapper、BookMapper、BookcopyMapper、BorrowrecMapper、CardrecMapper、FineMapper、PayrecMapper、DashboardMapper。',
    88: 'Controller 接收 HTTP 请求并调用 Service；Service 封装借还规则、证件生命周期、罚款计算等核心逻辑。新增 PortalController（门户首页）与 MeController（学生个人中心）。',
    92: 'Entity 与数据库表对应（Book、Student、Libcard、Borrowrec 等 9 个）；DTO 接收请求（BorrowDTO、LoginDTO、CardCreateDTO 等）；VO 封装响应（BookSearchVO、BorrowCheckVO、FineVO 等）。',
    95: '读者在前端 BorrowView 录入证号与条码 → 先 GET /api/cards/{cardNo}/borrow-check 校验资格 → POST /api/borrow → BorrowController → BorrowService.borrow()，校验通过后写入 borrowrec 并更新副本与证件计数。',
    102: '读者/管理员在前端 BorrowView 录入证号与条码 → POST /api/borrow → BorrowController → BorrowService.borrow()：',
    103: 'LibcardMapper.findByCardNo — 校验证件 cardStatus 为「正常」；',
    104: 'StudentMapper.findBySno — 获取学生 type 判定借阅上限；',
    105: 'FineMapper.sumUnpaidBySno — 校验未缴罚款 ≤ 50 元；',
    106: 'BorrowrecMapper.countActiveBySno — 校验在借数量上限；',
    107: 'BookcopyMapper / BookMapper — 校验副本可借且书目上架，更新副本 status；',
    108: 'BorrowrecMapper.insert + LibcardMapper.incrementTimes — 写入借阅记录并增加在借计数，返回应还日期（借出日+30天）。',
    113: ('登录后进入 MainLayout：左侧 Sidebar 可折叠导航（220px/64px，深色 #1E293B），'
          '顶部 Topbar 显示系统标题、当前用户姓名与角色 Tag，主内容区承载各功能页面。'
          '配色主色 #1E3A8A，表格统一 loading 与空数据提示。路由采用 Hash 模式便于静态部署。'),
    116: 'Tab 切换「管理员登录」与「学生登录（借书证号）」；表单校验密码 6–20 位；支持记住密码；登录后管理员跳转 /dashboard，学生跳转 /home 或 redirect 目标页。',
    119: '公共门户 HomePortalView（/home，无需登录）：展示热读推荐、借书明星、新书上架三栏榜单及顶部搜索框。管理员 Dashboard（/dashboard）展示 KPI 卡片与当月排行榜。',
    124: '顶部搜索框支持关键词检索；结果表格展示 ISBN、书名、作者、出版社、分类号；点击「详情」打开右侧 Drawer，显示简介及副本列表（过滤已注销副本），状态以 StatusBadge 彩色 Tag 区分。',
    128: ('借书页 BorrowView：Step1 输入证号并调用 borrow-check 校验（额度/罚款/证件状态）→ Step2 检索副本加入待借列表 → 批量借出。'
          '还书页 ReturnView：输入证号加载在借列表，超期行红色高亮，确认还书后显示罚款金额。两页均公开可访问。'),
    131: 'CardManageView 四个 Tab：新办、挂失、补办、注销；共用 StudentCardOpPanel 按学号查询；新办/补办支持证号预览（学号+001）；底部可查询办证历史。',
    137: ('采编管理拆分为三页：BookCatalogView 书目编目列表（分页搜索、下架）；'
          'BookFormView 新增/编辑书目（ISBN 编辑时不可改）；BookCopiesView 副本管理（新增/编辑位置/注销）。'
          '新书入库通过 POST /api/books/acquire 一次性完成书目+副本录入。'),
    144: 'PersonalView 四个 Tab：基本信息、借阅记录、缴纳罚款、修改密码；切换 Tab 懒加载数据，超期行高亮，支持多选批量缴纳未缴罚款。',
    152: ('通过本次高级 Java 课程设计，我们完成了基于 B/S 架构的图书管理系统的设计与实现。'
          '系统后端采用 Spring Boot 3.2.5、MyBatis 3.0.3 与 MySQL 8，前端采用 Vue 3.4、Vite 5 与 Element Plus 2.7，'
          '实现了图书采编与副本管理、借书证全生命周期管理、借还与超期罚款、图书挂失赔偿、'
          '双角色登录、公共门户首页及数据统计等核心功能，基本覆盖了需求文档中办公室、采编部、流通部与读者的主要业务环节。'),
    153: ('在开发过程中，我们进一步理解了分层架构的设计思想，掌握了 Controller、Service、Mapper 各层职责划分及 RESTful 接口设计；'
          '在借还、办证、合并缴款等业务中，通过 @Transactional 事务控制与 borrowrec/payrec 多表联动，'
          '加深了对数据库设计与业务规则落地的认识。前端采用 Pinia 管理登录态、Vue Router 导航守卫实现角色权限，'
          '公开借还工位设计贴近真实图书馆前台场景。'),
    154: ('本系统为课程设计演示版本，在密码加密、服务端鉴权（当前依赖请求头传递角色）等方面仍有优化空间。'
          'notice 公告表已建表但后端 API 尚未实现。后续可引入 Spring Security/JWT 加强安全机制，'
          '并扩展公告发布、自助预约等功能。总体而言，本次课设将理论知识与工程实践相结合，'
          '提升了团队协作与全栈开发能力。'),
}

FINES_SECTION_DESC = ('FineManageView：按缴纳状态筛选（全部/未缴/已缴）；表格展示学号、书名、超期天数、罚款金额与缴纳状态；'
                      '管理员手动输入证号，学生登录后证号自动填充；支持多选未缴记录合并缴纳。')

PARA_INSERT_AFTER_109 = ('前端采用 Vue 3 Composition API + Pinia 状态管理，按业务域划分 views 目录：'
                         'login（登录）、portal（门户）、borrow/return（流通工位）、dashboard（统计）、'
                         'card（证件）、book（采编/检索）、fine（罚款）、personal（个人中心）。'
                         'api/modules 下 8 个模块与后端 Controller 一一对应；layouts/MainLayout 提供 Sidebar+Topbar 布局。')

FIGURE_MAP = {
    63: ('fig4-1_er.png', Inches(6.2)),
    78: ('fig5-1_bs.png', Inches(5.5)),
    80: ('fig5-2_modules.png', Inches(6.0)),
    85: ('fig5-3_mapper.png', Inches(6.0)),
    90: ('fig5-4_controller_service.png', Inches(6.0)),
    96: ('fig5-5_sequence.png', Inches(6.2)),
    117: ('ui_login.png', Inches(5.8)),
    120: ('ui_portal.png', Inches(5.8)),
    121: ('ui_dashboard.png', Inches(5.8)),
    125: ('ui_book_search.png', Inches(5.8)),
    126: ('ui_book_search.png', Inches(5.8)),
    129: ('ui_borrow.png', Inches(5.8)),
    132: ('ui_card_create.png', Inches(5.8)),
    133: ('ui_card_loss.png', Inches(5.8)),
    134: ('ui_card_loss.png', Inches(5.8)),
    138: ('ui_book_catalog.png', Inches(5.8)),
    139: ('ui_book_form.png', Inches(5.8)),
    140: ('ui_book_copies.png', Inches(5.8)),
    142: ('ui_fines.png', Inches(5.8)),
    145: ('ui_personal.png', Inches(5.8)),
    146: ('ui_personal.png', Inches(5.8)),
    147: ('ui_personal.png', Inches(5.8)),
    148: ('ui_return.png', Inches(5.8)),
}

TABLE_UPDATES = {
    0: [  # 分层架构表
        ['层次', '后端包/目录', '前端目录', '主要职责'],
        ['表现层', '—', 'views/、layouts/、components/', '页面渲染、表单校验与交互'],
        ['控制层', 'com.library.controller（8个）', 'api/modules/（8个）', 'REST 接口路由与参数校验'],
        ['业务层', 'com.library.service（7个）', 'stores/auth.js', '业务规则、事务与状态管理'],
        ['数据层', 'com.library.mapper + entity', '—', 'MyBatis XML 映射 MySQL（9表）'],
    ],
    1: [  # 数据库表
        ['表名', '主键', '说明', '关键字段'],
        ['admin', 'id', '管理员', 'username, password, adminType(0证件/1采编/2流通)'],
        ['student', 'id', '学生学籍', 'sno, username, type, college, graduated'],
        ['libcard', 'cardNo', '借书证', 'sno, cardStatus(正常/挂失/注销), times'],
        ['cardrec', 'serNum', '证件操作流水', 'opType(新办/挂失/补办/注销), originCardNo, newCardNo'],
        ['book', 'id', '图书品种', 'ISBN, bname, author, bookStatus, price, clcNum'],
        ['bookcopy', 'barCode', '图书副本', 'ISBN, place, status(1可借/0借出/2注销)'],
        ['borrowrec', 'serNum', '借阅记录', 'retStatus, ovdDays, fineMoney, fineStatus, paySerNum'],
        ['payrec', 'serNum', '缴费流水', 'sno, payAmount, payDate'],
        ['notice', 'id', '公告（预留）', 'topic, content, author, createDate'],
    ],
    2: [  # 配置 - same mostly
        ['配置项', '默认值', '含义'],
        ['borrow-days', '30', '借期（天）'],
        ['fine-per-day', '1.00', '超期罚款单价（元/天）'],
        ['max-unpaid-fine', '50.00', '未缴罚款上限（元），超过禁止借书'],
        ['undergrad-borrow-limit', '5', '本科生最大在借数'],
        ['grad-borrow-limit', '10', '研究生最大在借数'],
    ],
    3: [  # 后端核心类
        ['类名', '类型', '职责'],
        ['AuthController / AuthService', '认证', '管理员用户名/学生证号双模式登录'],
        ['BookController / BookService', '图书', '检索、采编入库、下架、副本注销'],
        ['BorrowController / BorrowService', '借还', '批量借书、还书计罚、图书挂失赔偿'],
        ['CardController / CardService', '证件', '新办、挂失、补办、注销、借书资格校验'],
        ['FineController / FineService', '罚款', '查询（按角色过滤）、合并缴纳'],
        ['DashboardController / DashboardService', '统计', 'KPI 指标与当月排行榜'],
        ['MeController / MeService', '个人', '档案、密码、在借、历史、罚款、缴费'],
        ['PortalController', '门户', '热读/借书明星/新书上架'],
        ['GlobalExceptionHandler', '公共', '统一异常处理与 Result 响应'],
        ['LibraryProperties', '配置', '借阅规则参数外部化注入'],
    ],
    4: [  # 前端页面
        ['页面组件', '路由', '角色', '主要功能'],
        ['LoginView', '/login', '公开', '管理员/学生双 Tab 登录'],
        ['HomePortalView', '/home', '公开', '门户首页三榜单+搜索'],
        ['BorrowView', '/borrow', '公开', '资格校验+批量借书'],
        ['ReturnView', '/return', '公开', '按证号查在借+还书计罚'],
        ['DashboardView', '/dashboard', 'admin', 'KPI 卡片与排行榜'],
        ['CardManageView', '/cards', 'admin', '办证/挂失/补办/注销'],
        ['BookCatalogView', '/books/catalog', 'admin', '书目编目列表与下架'],
        ['BookFormView', '/books/add|/:isbn/edit', 'admin', '新增/编辑书目'],
        ['BookCopiesView', '/books/:isbn/copies', 'admin', '副本增删改'],
        ['BookSearchView', '/books/search', '已登录', '图书检索与详情抽屉'],
        ['FineManageView', '/fines', '已登录', '罚款查询与合并缴纳'],
        ['PersonalView', '/personal', 'student', '个人信息/借阅/罚款/改密'],
    ],
    5: [  # 技术栈
        ['类别', '技术/版本', '说明'],
        ['后端语言', 'Java 17', 'LTS 版本'],
        ['后端框架', 'Spring Boot 3.2.5', 'Web + 依赖注入 + Validation'],
        ['ORM', 'MyBatis 3.0.3', 'XML 映射 SQL，驼峰关闭'],
        ['数据库', 'MySQL 8.0+', 'library 库，9 张业务表'],
        ['API 文档', 'SpringDoc OpenAPI 2.5.0', '/swagger-ui.html'],
        ['前端框架', 'Vue 3.4 + Vite 5', 'Composition API + Hash 路由'],
        ['UI 库', 'Element Plus 2.7', '中文 locale + 图标库'],
        ['状态管理', 'Pinia 2.1', '登录态 localStorage 持久化'],
        ['HTTP', 'Axios 1.7', '请求头注入 X-Role/X-Sno/X-Card-No'],
        ['构建工具', 'Maven（后端）', '打包可执行 jar'],
    ],
}


def update_section_headings(doc):
    """更新部分标题"""
    heading_updates = {
        '6.2.2首页': '6.2.2门户首页与统计看板',
        '6.2.6图书采编（管理员）': '6.2.6图书采编管理（管理员）',
        '6.2.7学生罚款管理': '6.2.7罚款管理',
        '6.2.8个人查询': '6.2.8学生个人中心',
        '5.7 前端模块结构': '5.7 前端模块结构',
    }
    for p in doc.paragraphs:
        t = p.text.strip()
        if t in heading_updates:
            replace_paragraph_text(p, heading_updates[t])


def add_missing_paragraphs(doc):
    """在 5.7 节补充前端结构说明与图"""
    # Find paragraph with "5.7 前端模块结构"
    idx = None
    for i, p in enumerate(doc.paragraphs):
        if '5.7' in p.text and '前端' in p.text:
            idx = i
            break
    if idx is None:
        return

    # Check if next paragraph is empty (110)
    next_p = doc.paragraphs[idx + 1] if idx + 1 < len(doc.paragraphs) else None
    if next_p and not next_p.text.strip():
        replace_paragraph_text(next_p, PARA_INSERT_AFTER_109)

    # Insert figure after description if not present
    fig_path = os.path.join(DIAGRAM_DIR, 'fig5-6_frontend.png')
    if os.path.exists(fig_path):
        # find empty paragraph after 5.7 content
        for j in range(idx + 1, min(idx + 4, len(doc.paragraphs))):
            p = doc.paragraphs[j]
            if not p.text.strip() or '[FIGURE]' in str(p.runs):
                set_paragraph_image(p, fig_path, Inches(5.5))
                # add caption
                if j + 1 < len(doc.paragraphs):
                    cap = doc.paragraphs[j + 1]
                    if 'Caption' in (cap.style.name if cap.style else '') or not cap.text.strip():
                        replace_paragraph_text(cap, '图5-6  前端模块结构图')
                break


def main():
    if not os.path.exists(BACKUP):
        shutil.copy2(SRC_DOC, BACKUP)
        print('Backup created:', BACKUP)

    source = BACKUP if os.path.exists(BACKUP) else SRC_DOC
    doc = Document(source)
    print('Loaded document, paragraphs:', len(doc.paragraphs))

    for idx, text in PARA_UPDATES.items():
        if idx < len(doc.paragraphs):
            if text:
                replace_paragraph_text(doc.paragraphs[idx], text)
            elif idx == 56:
                replace_paragraph_text(doc.paragraphs[idx], '')

    for ti, rows in TABLE_UPDATES.items():
        if ti < len(doc.tables):
            update_table(doc.tables[ti], rows)

    for idx, (fname, width) in FIGURE_MAP.items():
        fpath = os.path.join(DIAGRAM_DIR, fname)
        if idx < len(doc.paragraphs) and os.path.exists(fpath):
            set_paragraph_image(doc.paragraphs[idx], fpath, width)

    update_section_headings(doc)
    add_missing_paragraphs(doc)

    # 在罚款截图前插入说明（不影响已替换的图片段落索引）
    for i, p in enumerate(doc.paragraphs):
        if p.text.strip() in ('6.2.7罚款管理', '6.2.7学生罚款管理'):
            # 若下一段已是说明则跳过
            if i + 1 < len(doc.paragraphs) and FINES_SECTION_DESC[:15] in doc.paragraphs[i + 1].text:
                break
            # 在标题后、截图前插入
            doc.paragraphs[i + 1].insert_paragraph_before(FINES_SECTION_DESC)
            break

    # Update 2.1 heading to mention portal
    for p in doc.paragraphs:
        if p.text.strip() == '2.1管理员模块':
            replace_paragraph_text(p, '2.1管理员与流通模块')

    try:
        doc.save(OUT_DOC_FINAL)
        print('Updated document saved:', OUT_DOC_FINAL)
    except PermissionError:
        doc.save(OUT_DOC)
        print('原文件被占用，已保存为:', OUT_DOC)
        print('请关闭 Word 后重新运行脚本，或手动将更新版重命名替换原文件。')


if __name__ == '__main__':
    main()
