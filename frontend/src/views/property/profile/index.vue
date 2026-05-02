<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-info">
            <el-avatar :size="80" icon="UserFilled" class="user-avatar" />
            <h3>{{ userInfo.realName || userInfo.username }}</h3>
            <p>
              <el-tag :type="getRoleType(userInfo.role)" size="small">
                {{ getRoleText(userInfo.role) }}
              </el-tag>
            </p>
          </div>
          <el-divider />
          <div class="user-meta">
            <div class="meta-item">
              <span class="label">用户名：</span>
              <span class="value">{{ userInfo.username }}</span>
            </div>
            <div class="meta-item">
              <span class="label">手机号：</span>
              <span class="value">{{ userInfo.phone || '未设置' }}</span>
            </div>
            <div class="meta-item">
              <span class="label">身份证号：</span>
              <span class="value">{{ maskIdCard(userInfo.idCard) }}</span>
            </div>
            <div class="meta-item">
              <span class="label">注册时间：</span>
              <span class="value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card class="form-card">
          <template #header>
            <span class="title">编辑个人信息</span>
          </template>
          
          <el-form 
            ref="formRef" 
            :model="form" 
            :rules="rules" 
            label-width="100px"
            class="edit-form"
          >
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleSave">保存修改</el-button>
              <el-button @click="loadUserInfo">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card class="action-card" style="margin-top: 20px;">
          <template #header>
            <span class="title">账号操作</span>
          </template>
          
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const userInfo = computed(() => userStore.userInfo)

const form = reactive({
  realName: '',
  phone: '',
  idCard: ''
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

const roleMap = {
  RESIDENT: { text: '居民', type: 'success' },
  PROPERTY: { text: '物业人员', type: 'primary' },
  ADMIN: { text: '管理员', type: 'danger' }
}

const getRoleText = (role) => roleMap[role]?.text || role
const getRoleType = (role) => roleMap[role]?.type || ''

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const maskIdCard = (idCard) => {
  if (!idCard) return '未设置'
  if (idCard.length === 18) {
    return idCard.substring(0, 6) + '********' + idCard.substring(14)
  }
  return idCard
}

const loadUserInfo = () => {
  if (userInfo.value) {
    form.realName = userInfo.value.realName || ''
    form.phone = userInfo.value.phone || ''
    form.idCard = userInfo.value.idCard || ''
  }
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const data = {
      id: userInfo.value.id,
      realName: form.realName,
      phone: form.phone,
      idCard: form.idCard
    }
    
    await userStore.updateUserInfo(data)
  } finally {
    loading.value = false
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadUserInfo()
})

watch(
  () => userInfo.value,
  () => {
    loadUserInfo()
  },
  { deep: true }
)
</script>

<style lang="scss" scoped>
.profile-page {
  .user-card {
    .user-info {
      text-align: center;
      
      .user-avatar {
        background: #f5f7fa;
      }
      
      h3 {
        margin: 15px 0 10px 0;
        font-size: 18px;
      }
    }
    
    .user-meta {
      .meta-item {
        display: flex;
        margin-bottom: 12px;
        
        .label {
          color: #909399;
          width: 80px;
        }
        
        .value {
          color: #333;
          flex: 1;
        }
      }
    }
  }
  
  .form-card,
  .action-card {
    .title {
      font-size: 16px;
      font-weight: 600;
    }
    
    .edit-form {
      max-width: 500px;
    }
  }
}
</style>