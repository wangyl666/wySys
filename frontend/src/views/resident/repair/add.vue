<template>
  <div class="repair-add-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="title">提交报修</span>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        class="repair-form"
      >
        <el-form-item label="报修标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入报修标题" maxlength="100" />
        </el-form-item>

        <el-form-item label="报修类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择报修类型" style="width: 100%">
            <el-option label="水电维修" value="水电维修" />
            <el-option label="家电维修" value="家电维修" />
            <el-option label="管道疏通" value="管道疏通" />
            <el-option label="房屋维修" value="房屋维修" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="报修内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="5" 
            placeholder="请详细描述报修内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="维修地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入维修地址（如：1号楼1单元101室）" />
        </el-form-item>

        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="期望维修时间" prop="preferredTime">
          <el-date-picker
            v-model="form.preferredTime"
            type="datetime"
            placeholder="请选择期望维修时间"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  title: '',
  type: '',
  content: '',
  address: '',
  contactName: '',
  contactPhone: '',
  preferredTime: null
})

const rules = {
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择报修类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入报修内容', trigger: 'blur' }],
  address: [{ required: true, message: '请输入维修地址', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await request.post('/api/repair', form)
    if (res.code === 200) {
      ElMessage.success('报修提交成功')
      router.push('/resident/repair')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 可以从用户信息中获取默认联系人和电话
})
</script>

<style lang="scss" scoped>
.repair-add-page {
  .card-header {
    display: flex;
    align-items: center;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      margin-left: 10px;
    }
  }
  
  .repair-form {
    max-width: 600px;
    padding: 20px 0;
  }
}
</style>