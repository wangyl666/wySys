<template>
  <div class="repair-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部类型" clearable>
            <el-option label="水电维修" value="水电维修" />
            <el-option label="家电维修" value="家电维修" />
            <el-option label="管道疏通" value="管道疏通" />
            <el-option label="房屋维修" value="房屋维修" />
            <el-option label="其他" value="其他" />
          </el-select>
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
        <el-table-column prop="title" label="报修标题" width="180">
          <template #default="{ row }">
            <span class="title-text">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="realName" label="业主" width="100" />
        <el-table-column prop="address" label="地址" width="150" />
        <el-table-column prop="contactName" label="联系人" width="80" />
        <el-table-column prop="staffName" label="处理人" width="80">
          <template #default="{ row }">
            {{ row.staffName || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row.id)">
              详情
            </el-button>
            <el-button 
              type="success" 
              link 
              size="small" 
              @click="handleAssign(row)"
              v-if="row.status === 'PENDING'"
            >
              派单
            </el-button>
            <el-button 
              type="warning" 
              link 
              size="small" 
              @click="handleComplete(row)"
              v-if="row.status === 'PROCESSING'"
            >
              完成
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

    <el-dialog v-model="assignDialogVisible" title="派单给维修人员" width="400px">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="80px">
        <el-form-item label="维修人员" prop="staffId">
          <el-select 
            v-model="assignForm.staffId" 
            placeholder="请选择维修人员" 
            style="width: 100%"
            @change="handleStaffChange"
          >
            <el-option 
              v-for="staff in staffList" 
              :key="staff.id" 
              :label="staff.realName" 
              :value="staff.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="assignForm.staffPhone" placeholder="自动填充" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="confirmAssign">确认派单</el-button>
      </template>
    </el-dialog>
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
const assignDialogVisible = ref(false)
const assigning = ref(false)
const assignFormRef = ref(null)

const queryForm = reactive({
  status: '',
  type: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const staffList = ref([])
const assignForm = reactive({
  currentId: null,
  staffId: null,
  staffName: '',
  staffPhone: ''
})

const assignRules = {
  staffId: [{ required: true, message: '请选择维修人员', trigger: 'change' }]
}

const statusMap = {
  PENDING: { text: '待处理', type: 'warning' },
  PROCESSING: { text: '处理中', type: 'primary' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (queryForm.status) params.status = queryForm.status
    if (queryForm.type) params.type = queryForm.type
    
    const res = await request.get('/api/repair/page', { params })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const loadStaffList = async () => {
  try {
    const res = await request.get('/api/user/page', {
      params: { role: 'PROPERTY', size: 100 }
    })
    if (res.code === 200) {
      staffList.value = res.data.records
    }
  } catch (error) {
    console.error('加载维修人员列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  queryForm.status = ''
  queryForm.type = ''
  handleSearch()
}

const viewDetail = (id) => {
  router.push(`/property/repair/detail/${id}`)
}

const handleAssign = (row) => {
  assignForm.currentId = row.id
  assignForm.staffId = null
  assignForm.staffName = ''
  assignForm.staffPhone = ''
  assignDialogVisible.value = true
}

const handleStaffChange = (staffId) => {
  const staff = staffList.value.find(item => item.id === staffId)
  if (staff) {
    assignForm.staffName = staff.realName
    assignForm.staffPhone = staff.phone
  }
}

const confirmAssign = async () => {
  const valid = await assignFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  assigning.value = true
  try {
    const res = await request.put(`/api/repair/assign/${assignForm.currentId}`, null, {
      params: {
        staffId: assignForm.staffId,
        staffName: assignForm.staffName,
        staffPhone: assignForm.staffPhone
      }
    })
    if (res.code === 200) {
      ElMessage.success('派单成功')
      assignDialogVisible.value = false
      loadData()
    }
  } finally {
    assigning.value = false
  }
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要完成该报修吗？完成后将无法撤销。', '提示', {
      confirmButtonText: '确定完成',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.put(`/api/repair/complete/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('操作成功')
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
  loadStaffList()
})
</script>

<style lang="scss" scoped>
.repair-page {
  .filter-card {
    margin-bottom: 20px;
  }
  
  .query-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }
  
  .title-text {
    display: inline-block;
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>