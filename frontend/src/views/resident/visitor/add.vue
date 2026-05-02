<template>
  <div class="visitor-add-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="title">预约访客</span>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        class="visitor-form"
      >
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input v-model="form.visitorName" placeholder="请输入访客姓名" />
        </el-form-item>

        <el-form-item label="访客电话" prop="visitorPhone">
          <el-input v-model="form.visitorPhone" placeholder="请输入访客电话" />
        </el-form-item>

        <el-form-item label="身份证号" prop="visitorIdCard">
          <el-input v-model="form.visitorIdCard" placeholder="请输入身份证号（选填）" />
        </el-form-item>

        <el-form-item label="访客人数" prop="visitorCount">
          <el-input-number v-model="form.visitorCount" :min="1" :max="10" />
        </el-form-item>

        <el-form-item label="来访事由" prop="visitReason">
          <el-select v-model="form.visitReason" placeholder="请选择来访事由" style="width: 100%">
            <el-option label="亲友探访" value="亲友探访" />
            <el-option label="快递配送" value="快递配送" />
            <el-option label="家政服务" value="家政服务" />
            <el-option label="维修服务" value="维修服务" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="访问地址" prop="visitAddress">
          <el-input v-model="form.visitAddress" placeholder="请输入访问地址（如：1号楼1单元101室）" />
        </el-form-item>

        <el-form-item label="来访时间" prop="visitTime">
          <el-date-picker
            v-model="form.visitTime"
            type="datetime"
            placeholder="请选择来访时间"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交预约</el-button>
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

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  visitorName: '',
  visitorPhone: '',
  visitorIdCard: '',
  visitorCount: 1,
  visitReason: '',
  visitAddress: '',
  visitTime: null
})

const rules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  visitorPhone: [
    { required: true, message: '请输入访客电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  visitorCount: [{ required: true, message: '请输入访客人数', trigger: 'blur' }],
  visitReason: [{ required: true, message: '请选择来访事由', trigger: 'change' }],
  visitAddress: [{ required: true, message: '请输入访问地址', trigger: 'blur' }],
  visitTime: [{ required: true, message: '请选择来访时间', trigger: 'change' }]
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await request.post('/api/visitor', form)
    if (res.code === 200) {
      ElMessage.success('预约提交成功')
      router.push('/resident/visitor')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 可以从用户信息中获取默认地址
})
</script>

<style lang="scss" scoped>
.visitor-add-page {
  .card-header {
    display: flex;
    align-items: center;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      margin-left: 10px;
    }
  }
  
  .visitor-form {
    max-width: 600px;
    padding: 20px 0;
  }
}
</style>