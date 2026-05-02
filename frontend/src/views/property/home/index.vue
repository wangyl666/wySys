<template>
  <div class="property-home">
    <div class="stat-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon pending-repair">
                <el-icon :size="32"><Tools /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingRepairCount }}</div>
                <div class="stat-label">待处理报修</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon pending-approval">
                <el-icon :size="32"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingVisitorCount }}</div>
                <div class="stat-label">待审核访客</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon unpaid-fee">
                <el-icon :size="32"><Wallet /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.unpaidBillCount }}</div>
                <div class="stat-label">未缴费账单</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon notice">
                <el-icon :size="32"><Bell /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.noticeCount }}</div>
                <div class="stat-label">已发布公告</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">最新报修</span>
              <el-link type="primary" @click="$router.push('/property/repair')">查看更多</el-link>
            </div>
          </template>
          
          <el-table :data="recentRepairs" v-loading="loading" stripe max-height="400">
            <el-table-column prop="title" label="报修标题" width="200">
              <template #default="{ row }">
                <span class="title-text">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="realName" label="业主" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewRepairDetail(row.id)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">待审核访客</span>
              <el-link type="primary" @click="$router.push('/property/visitor')">查看更多</el-link>
            </div>
          </template>
          
          <el-table :data="pendingVisitors" v-loading="loading" stripe max-height="400">
            <el-table-column prop="visitorName" label="访客姓名" width="100" />
            <el-table-column prop="visitorPhone" label="访客电话" width="120" />
            <el-table-column prop="realName" label="业主" width="100" />
            <el-table-column prop="visitReason" label="来访事由" width="100" />
            <el-table-column prop="visitTime" label="来访时间" width="140">
              <template #default="{ row }">
                {{ formatDate(row.visitTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="success" link size="small" @click="handleApprove(row)">
                  审核
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const stats = reactive({
  pendingRepairCount: 0,
  pendingVisitorCount: 0,
  unpaidBillCount: 0,
  noticeCount: 0
})

const recentRepairs = ref([])
const pendingVisitors = ref([])

const statusMap = {
  PENDING: { text: '待处理', type: 'warning' },
  PROCESSING: { text: '处理中', type: 'primary' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'

const formatDate = (date) => {
  return date ? dayjs(date).format('MM-DD HH:mm') : '-'
}

const loadStats = async () => {
  try {
    const [repairRes, visitorRes, billRes, noticeRes] = await Promise.all([
      request.get('/api/repair/page', { params: { status: 'PENDING', size: 1 } }),
      request.get('/api/visitor/page', { params: { status: 'PENDING', size: 1 } }),
      request.get('/api/fee-bill/page', { params: { status: 'UNPAID', size: 1 } }),
      request.get('/api/notice/page', { params: { status: 1, size: 1 } })
    ])
    
    stats.pendingRepairCount = repairRes.data?.total || 0
    stats.pendingVisitorCount = visitorRes.data?.total || 0
    stats.unpaidBillCount = billRes.data?.total || 0
    stats.noticeCount = noticeRes.data?.total || 0
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentRepairs = async () => {
  try {
    const res = await request.get('/api/repair/page', { params: { current: 1, size: 5 } })
    if (res.code === 200) {
      recentRepairs.value = res.data.records
    }
  } catch (error) {
    console.error('加载最新报修失败:', error)
  }
}

const loadPendingVisitors = async () => {
  try {
    const res = await request.get('/api/visitor/page', { params: { status: 'PENDING', current: 1, size: 5 } })
    if (res.code === 200) {
      pendingVisitors.value = res.data.records
    }
  } catch (error) {
    console.error('加载待审核访客失败:', error)
  }
}

const viewRepairDetail = (id) => {
  router.push(`/property/repair/detail/${id}`)
}

const handleApprove = async (row) => {
  try {
    const { value: action } = await ElMessageBox.confirm(
      `访客：${row.visitorName}，来访事由：${row.visitReason}`,
      '审核访客预约',
      {
        confirmButtonText: '通过',
        cancelButtonText: '拒绝',
        inputPlaceholder: '请输入审核意见（选填）',
        showInput: true,
        type: 'info'
      }
    ).catch(({ action }) => action)
    
    if (action === 'confirm') {
      const res = await request.put(`/api/visitor/approve/${row.id}`, null, {
        params: { auditComment: '审核通过' }
      })
      if (res.code === 200) {
        ElMessage.success('审核通过')
        loadPendingVisitors()
        loadStats()
      }
    } else if (action === 'cancel') {
      const { value: comment } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝原因', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入拒绝原因'
      }).catch(() => null)
      
      if (comment) {
        const res = await request.put(`/api/visitor/reject/${row.id}`, null, {
          params: { auditComment: comment }
        })
        if (res.code === 200) {
          ElMessage.success('已拒绝')
          loadPendingVisitors()
          loadStats()
        }
      }
    }
  } catch (error) {
    console.error('审核操作失败:', error)
  }
}

onMounted(() => {
  loading.value = true
  Promise.all([loadStats(), loadRecentRepairs(), loadPendingVisitors()]).finally(() => {
    loading.value = false
  })
})
</script>

<style lang="scss" scoped>
.property-home {
  .stat-cards {
    margin-bottom: 20px;
  }
  
  .stat-card {
    :deep(.el-card__body) {
      padding: 20px;
    }
  }
  
  .stat-content {
    display: flex;
    align-items: center;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      
      &.pending-repair {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
      
      &.pending-approval {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
      
      &.unpaid-fee {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
      
      &.notice {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }
    }
    
    .stat-info {
      margin-left: 20px;
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #333;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 5px;
      }
    }
  }
  
  .list-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .card-title {
      font-size: 16px;
      font-weight: 600;
    }
    
    .title-text {
      display: inline-block;
      max-width: 180px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>