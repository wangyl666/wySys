<template>
  <div class="visitor-detail-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="title">访客详情</span>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else class="detail-content">
        <div class="detail-header">
          <h3>访客：{{ detail.visitorName }}</h3>
          <el-tag :type="getStatusType(detail.status)" size="large">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="访客姓名">{{ detail.visitorName }}</el-descriptions-item>
          <el-descriptions-item label="访客电话">{{ detail.visitorPhone }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ detail.visitorIdCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="访客人数">{{ detail.visitorCount || 1 }}人</el-descriptions-item>
          <el-descriptions-item label="来访事由">{{ detail.visitReason }}</el-descriptions-item>
          <el-descriptions-item label="访问地址">{{ detail.visitAddress }}</el-descriptions-item>
          <el-descriptions-item label="来访时间">{{ formatDate(detail.visitTime) }}</el-descriptions-item>
          <el-descriptions-item label="离开时间">{{ formatDate(detail.leaveTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="业主姓名">{{ detail.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="业主电话">{{ detail.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预约时间" :span="2">
            {{ formatDate(detail.createTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.auditName" class="audit-section">
          <h4>审核信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="审核人">{{ detail.auditName }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ formatDate(detail.auditTime) }}</el-descriptions-item>
            <el-descriptions-item label="审核意见" :span="2">
              {{ detail.auditComment || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="detail.checkInTime" class="check-section">
          <h4>签到签退信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="签到时间">{{ formatDate(detail.checkInTime) }}</el-descriptions-item>
            <el-descriptions-item label="签退时间">{{ formatDate(detail.checkOutTime) || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="action-section">
          <el-button 
            type="success" 
            @click="handleApprove"
            v-if="detail.status === 'PENDING'"
          >
            审核通过
          </el-button>
          <el-button 
            type="danger" 
            @click="handleReject"
            v-if="detail.status === 'PENDING'"
          >
            审核拒绝
          </el-button>
          <el-button 
            type="warning" 
            @click="handleCheckIn"
            v-if="detail.status === 'APPROVED'"
          >
            签到
          </el-button>
          <el-button 
            type="info" 
            @click="handleCheckOut"
            v-if="detail.status === 'CHECKED_IN'"
          >
            签退
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const detail = ref({})

const statusMap = {
  PENDING: { text: '待审核', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' },
  CHECKED_IN: { text: '已签到', type: 'primary' },
  CHECKED_OUT: { text: '已签退', type: 'info' },
  CANCELLED: { text: '已取消', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/visitor/${route.params.id}`)
    if (res.code === 200) {
      detail.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const handleApprove = async () => {
  try {
    const { value: comment } = await ElMessageBox.prompt(
      '请输入审核意见（选填）',
      '通过审核',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入审核意见（选填）'
      }
    ).catch(() => null)
    
    if (comment !== undefined) {
      const res = await request.put(`/api/visitor/approve/${route.params.id}`, null, {
        params: { auditComment: comment || '审核通过' }
      })
      if (res.code === 200) {
        ElMessage.success('审核通过')
        loadDetail()
      }
    }
  } catch (error) {
    console.error('审核失败:', error)
  }
}

const handleReject = async () => {
  try {
    const { value: comment } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '拒绝审核',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入拒绝原因'
      }
    ).catch(() => null)
    
    if (comment) {
      const res = await request.put(`/api/visitor/reject/${route.params.id}`, null, {
        params: { auditComment: comment }
      })
      if (res.code === 200) {
        ElMessage.success('已拒绝')
        loadDetail()
      }
    }
  } catch (error) {
    console.error('拒绝操作失败:', error)
  }
}

const handleCheckIn = async () => {
  try {
    await ElMessageBox.confirm('确定要签到该访客吗？', '签到确认', {
      confirmButtonText: '确定签到',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const res = await request.put(`/api/visitor/checkin/${route.params.id}`)
    if (res.code === 200) {
      ElMessage.success('签到成功')
      loadDetail()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleCheckOut = async () => {
  try {
    await ElMessageBox.confirm('确定要签退该访客吗？', '签退确认', {
      confirmButtonText: '确定签退',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const res = await request.put(`/api/visitor/checkout/${route.params.id}`)
    if (res.code === 200) {
      ElMessage.success('签退成功')
      loadDetail()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.visitor-detail-page {
  .card-header {
    display: flex;
    align-items: center;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      margin-left: 10px;
    }
  }
  
  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 0;
    color: #909399;
    
    span {
      margin-top: 10px;
    }
  }
  
  .detail-content {
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        margin: 0;
        font-size: 18px;
      }
    }
    
    .audit-section,
    .check-section,
    .action-section {
      margin-top: 20px;
      
      h4 {
        margin: 0 0 15px 0;
        font-size: 16px;
        padding-bottom: 10px;
        border-bottom: 1px solid #f0f0f0;
      }
    }
  }
}
</style>