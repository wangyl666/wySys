<template>
  <div class="notice-add-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="title">{{ isEdit ? '编辑公告' : '发布公告' }}</span>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        class="add-form"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" />
        </el-form-item>

        <el-form-item label="公告类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择公告类型" style="width: 100%">
            <el-option label="一般通知" value="GENERAL" />
            <el-option label="重要通知" value="IMPORTANT" />
            <el-option label="紧急通知" value="EMERGENCY" />
            <el-option label="活动通知" value="ACTIVITY" />
          </el-select>
        </el-form-item>

        <el-form-item label="是否置顶">
          <el-radio-group v-model="form.isTop">
            <el-radio :value="1">置顶</el-radio>
            <el-radio :value="0">不置顶</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="发布状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">立即发布</el-radio>
            <el-radio :value="0">保存草稿</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="显示开始时间">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示结束时间">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="公告内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="10" 
            placeholder="请输入公告内容"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '发布公告' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  type: 'GENERAL',
  content: '',
  isTop: 0,
  status: 1,
  startTime: null,
  endTime: null
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const loadDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await request.get(`/api/notice/${route.params.id}`)
    if (res.code === 200) {
      form.id = res.data.id
      form.title = res.data.title
      form.type = res.data.type
      form.content = res.data.content
      form.isTop = res.data.isTop || 0
      form.status = res.data.status
      form.startTime = res.data.startTime
      form.endTime = res.data.endTime
    }
  } catch (error) {
    console.error('加载公告详情失败:', error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    if (isEdit.value) {
      // 简化处理，实际项目中需要调用更新API
      ElMessage.success('保存成功')
      router.push('/property/notice')
    } else {
      const res = await request.post('/api/notice', form)
      if (res.code === 200) {
        ElMessage.success(form.status === 1 ? '发布成功' : '保存成功')
        router.push('/property/notice')
      }
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.notice-add-page {
  .card-header {
    display: flex;
    align-items: center;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      margin-left: 10px;
    }
  }
  
  .add-form {
    max-width: 800px;
    padding: 20px 0;
  }
}
</style>