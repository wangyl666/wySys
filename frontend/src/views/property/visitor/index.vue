<template>
  <div class="visitor-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已签到" value="CHECKED_IN" />
            <el-option label="已签退" value="CHECKED_OUT" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="访客姓名">
          <el-input v-model="queryForm.visitorName" placeholder="访客姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="visitorName" label="访客姓名" width="100" />
        <el-table-column prop="visitorPhone" label="访客电话" width="120" />
        <el-table-column prop="visitorCount" label="人数" width="80">
          <template #default="{ row }">
            {{ row.visitorCount || 1 }}人
          </template>
        </el-table-column>
        <el-table-column prop="visitReason" label="事由" width="100" />
        <el-table-column prop="visitAddress" label="访问地址" width="150" />
        <el-table-column prop="realName" label="业主" width="100" />
        <el-table-column prop="visitTime" label="来访时间" width="140">
          <template #default="{ row }">
            {{ formatDate(row.visitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row.id)">
              详情
            </el-button>
            <el-button 
              type="success" 
              link 
              size="small" 
              @click="handleApprove(row)"
              v-if="row.status === 'PENDING'"
            >
              通过
            </el-button>
            <el-button 
              type="danger" 
              link 
              size="small" 
              @click="handleReject(row)"
              v-if="row.status === 'PENDING'"
            >
              拒绝
            </el-button>
            <el-button 
              type="warning" 
              link 
              size="small" 
              @click="handleCheckIn(row)"
              v-if="row.status === 'APPROVED'"
            >
              签到
            </el-button>
            <el-button 
              type="info" 
              link 
              size="small" 
              @click="handleCheckOut(row)"
              v-if="row.status === 'CHECKED_IN'"
            >
              签退
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])

const queryForm = reactive({
  status: '',
  visitorName: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

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
  return date ? dayjs(date).format('MM-DD HH:mm') : '-'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (queryForm.status) params.status = queryForm.status
    if (queryForm.visitorName) params.visitorName = queryForm.visitorName
    
    const res = await request.get('/api/visitor/page', { params })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  queryForm.status = ''
  queryForm.visitorName = ''
  handleSearch()
}

const viewDetail = (id) => {
  router.push(`/property/visitor/detail/${id}`)
}

const handleApprove = async (row) => {
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
      const res = await request.put(`/api/visitor/approve/${row.id}`, null, {
        params: { auditComment: comment || '审核通过' }
      })
      if (res.code === 200) {
        ElMessage.success('审核通过')
        loadData()
      }
    }
  } catch (error) {
    console.error('审核失败:', error)
  }
}

const handleReject = async (row) => {
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
      const res = await request.put(`/api/visitor/reject/${row.id}`, null, {
        params: { auditComment: comment }
      })
      if (res.code === 200) {
        ElMessage.success('已拒绝')
        loadData()
      }
    }
  } catch (error) {
    console.error('拒绝操作失败:', error)
  }
}

const handleCheckIn = async (row) => {
  try {
    await ElMessageBox.confirm('确定要签到该访客吗？', '签到确认', {
      confirmButtonText: '确定签到',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const res = await request.put(`/api/visitor/checkin/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('签到成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleCheckOut = async (row) => {
  try {
    await ElMessageBox.confirm('确定要签退该访客吗？', '签退确认', {
      confirmButtonText: '确定签退',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const res = await request.put(`/api/visitor/checkout/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('签退成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.visitor-page {
  .filter-card {
    margin-bottom: 20px;
  }
  
  .query-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>