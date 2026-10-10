<template>
  <!-- 整体页面容器 -->
  <div class="page-container">
    <!-- 顶部标题栏：根据编辑/新增模式切换标题文字 -->
    <div class="page-header">
      <h2 class="page-title">{{ isEdit ? '✏️ 编辑书目' : '📖 新增书目' }}</h2>
      <el-button @click="goBack">返回列表</el-button>
    </div>

    <!-- 表单卡片区域 -->
    <el-card shadow="never" class="card-wrapper">
      <!-- 书目信息表单，绑定校验规则 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <!-- 第一行：ISBN / 书名 / 作者 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 编辑模式下 ISBN 不可修改 -->
            <el-form-item label="ISBN" prop="ISBN">
              <el-input v-model="form.ISBN" placeholder="10位或13位ISBN" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="书名" prop="bookName">
              <el-input v-model="form.bookName" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二行：出版社 / 出版日期 / 分类号 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 日期选择器，值格式为 YYYY-MM-DD -->
            <el-form-item label="出版日期" prop="pubDate">
              <el-date-picker
                v-model="form.pubDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类号" prop="category">
              <el-input v-model="form.category" placeholder="如 TP312" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行：图书简介（多行文本域） -->
        <el-form-item label="介绍" prop="introduction">
          <el-input v-model="form.introduction" type="textarea" :rows="3" placeholder="请输入图书简介" />
        </el-form-item>
      </el-form>

      <!-- 底部操作按钮栏 -->
      <div class="action-bar">
        <!-- 提交按钮，编辑时显示"保存修改"，新增时显示"确认新增" -->
        <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '确认新增' }}
        </el-button>
        <el-button size="large" @click="goBack">取消</el-button>
        <!-- 仅编辑模式下显示"管理副本"入口 -->
        <el-button v-if="isEdit" type="success" size="large" @click="goCopies">管理副本</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 书目新增/编辑共用表单页。URL 含 isbn 参数时为编辑模式（ISBN 字段不可改），否则为新增。
 * 新增成功后跳转到副本管理页，方便紧接着录入副本。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { acquireBook, getBookDetail, updateBook } from '@/api/modules/books'

// 当前路由实例，用于读取路径参数和查询参数
const route = useRoute()
// 路由实例，用于编程式跳转
const router = useRouter()
// 是否为编辑模式：路由中存在 isbn 参数即为编辑
const isEdit = computed(() => !!route.params.isbn)
// 从路由参数中取出的 isbn 值
const isbn = computed(() => route.params.isbn)

// 表单组件引用，用于手动触发表单校验
const formRef = ref(null)
// 提交中状态，控制按钮 loading
const submitting = ref(false)

// 表单数据对象，双向绑定到各输入框
const form = reactive({
  ISBN: '',
  bookName: '',
  author: '',
  publisher: '',
  pubDate: '',
  category: '',
  introduction: '',
})

// 表单校验规则：ISBN 格式 / 必填项
const rules = {
  ISBN: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    { pattern: /^\d{9}[\dX]$|^\d{13}$/, message: 'ISBN格式不正确（10位或13位）', trigger: 'blur' },
  ],
  bookName: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  publisher: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
  pubDate: [{ required: true, message: '请选择出版日期', trigger: 'change' }],
}

// 编辑模式下加载书目详情，回填到表单
async function loadDetail() {
  if (!isEdit.value) return // 新增模式不加载
  const res = await getBookDetail(isbn.value)
  const data = res.data
  // 把接口返回的字段逐一赋值给 form
  Object.assign(form, {
    ISBN: data.ISBN,
    bookName: data.bookName,
    author: data.author,
    publisher: data.publisher,
    pubDate: data.pubDate,
    category: data.category,
    introduction: data.introduction || '',
  })
}

// 提交表单：先校验再调接口，编辑走 updateBook，新增走 acquireBook
async function handleSubmit() {
  let valid = false
  try {
    valid = await formRef.value.validate()
  } catch {
    /* 校验失败会抛异常，这里静默处理 */
  }
  if (!valid) return // 校验不通过直接返回

  submitting.value = true // 开启按钮 loading
  try {
    if (isEdit.value) {
      // 编辑模式：调更新接口，成功后回目录页
      await updateBook(isbn.value, {
        bookName: form.bookName,
        author: form.author,
        publisher: form.publisher,
        pubDate: form.pubDate,
        category: form.category,
        introduction: form.introduction,
      })
      ElMessage.success('保存成功')
      router.push('/books/catalog')
    } else {
      // 新增模式：调采购接口，成功后跳副本管理页
      await acquireBook({ ...form, copies: [] })
      ElMessage.success('新增成功，可继续添加副本')
      router.push(`/books/${form.ISBN}/copies`)
    }
  } finally {
    submitting.value = false // 无论成功失败都关闭 loading
  }
}

// 返回图书目录列表页
function goBack() {
  router.push('/books/catalog')
}

// 跳转到当前书目的副本管理页
function goCopies() {
  router.push(`/books/${isbn.value}/copies`)
}

// 页面挂载后：编辑模式加载详情；新增模式若携带 keyword 则自动预填
onMounted(async () => {
  if (isEdit.value) {
    await loadDetail()
  // 新增模式下，如果从目录页带了 keyword 过来，自动预填 ISBN 或书名
  } else if (route.query.keyword) {
    const kw = String(route.query.keyword)
    // 纯数字且长度符合 ISBN 格式就填到 ISBN 字段，否则当作书名
    if (/^\d{10}$|^\d{13}$/.test(kw)) {
      form.ISBN = kw
    } else {
      form.bookName = kw
    }
  }
})
</script>

<style scoped>
/* 顶部标题栏：标题与返回按钮水平排列 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

/* 去掉标题默认的下边距和底线，让布局更紧凑 */
.page-header .page-title {
  margin-bottom: 0;
  border-bottom: none;
  padding-bottom: 0;
}
</style>
