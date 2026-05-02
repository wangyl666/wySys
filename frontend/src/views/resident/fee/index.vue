<template>
  <div class="fee-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable>
            <el-option label="未缴费" value="UNPAID" />
            <el-option label="部分缴费" value="PARTIAL" />
            <el-option label="已缴费" value="PAID" />
            <el-option label="已逾期" value="OVERDUE" />
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
      <template #header>
        <div class="card-header">
          <span class="title">账单列表</span>
          <el-button 
            type="primary" 
            :disabled="selectedBills.length === 0"
            @click="handleBatchPay"
          >
            批量缴费（{{ selectedBills.length }}）
          </el-button>
        </div>
      </template>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50">
          <template #default="{ row }">
            <el-checkbox 
              v-model="row.selected" 
              :disabled="row.status === 'PAID'"
              @change="handleRowChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="billNo" label="账单编号" width="180" />
        <el-table-column prop="feeTypeName" label="费用类型" width="120" />
        <el-table-column prop="billMonth" label="账单月份" width="100" />
        <el-table-column label="房屋信息" width="180">
          <template #default="{ row }">
            {{ row.buildingNo }}{{ row.unitNo || '' }}{{ row.roomNo || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lateFee" label="滞纳金" width="100">
          <template #default="{ row }">
            <span class="late-fee" v-if="row.lateFee > 0">¥{{ row.lateFee }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="应付总额" width="120">
          <template #default="{ row }">
            <span class="total-amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payableDate" label="应缴日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.payableDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row.id)">详情</el-button>
            <el-button 
              type="success" 
              link 
              @click="handlePay(row)" 
              v-if="row.status !== 'PAID'"
            >
              缴费
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

    <el-dialog v-model="payDialogVisible" title="缴费确认" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="账单编号">{{ currentBill.billNo }}</el-descriptions-item>
        <el-descriptions-item label="费用类型">{{ currentBill.feeTypeName }}</el-descriptions-item>
        <el-descriptions-item label="应付金额">
          <span class="dialog-amount">¥{{ currentBill.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-select v-model="payMethod" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="微信支付" value="微信支付" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="银行卡" value="银行卡" />
          </el-select>
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="confirmPay">确认支付</el-button>
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
const selectedBills = ref([])
const payDialogVisible = ref(false)
const currentBill = ref({})
const payMethod = ref('微信支付')
const payLoading = ref(false)

const queryForm = reactive({
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const statusMap = {
  UNPAID: { text: '未缴费', type: 'warning' },
  PARTIAL: { text: '部分缴费', type: 'primary' },
  PAID: { text: '已缴费', type: 'success' },
  OVERDUE: { text: '已逾期', type: 'danger' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (queryForm.status) params.status = queryForm.status
    
    const res = await request.get('/api/fee-bill/page/my', { params })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
      selectedBills.value = []
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
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedBills.value = selection.filter(item => item.status !== 'PAID')
}

const handleRowChange = (row) => {
  if (row.selected) {
    if (!selectedBills.value.find(item => item.id === row.id)) {
      selectedBills.value.push(row)
    }
  } else {
    selectedBills.value = selectedBills.value.filter(item => item.id !== row.id)
  }
}

const viewDetail = (id) => {
  router.push(`/resident/fee/detail/${id}`)
}

const handlePay = (row) => {
  currentBill.value = row
  payDialogVisible.value = true
}

const confirmPay = async () => {
  if (!payMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  payLoading.value = true
  try {
    const res = await request.post(`/api/fee-bill/pay/${currentBill.value.id}`, null, {
      params: {
        paymentMethod: payMethod.value
      }
    })
    if (res.code === 200) {
      ElMessage.success('缴费成功')
      payDialogVisible.value = false
      loadData()
    }
  } finally {
    payLoading.value = false
  }
}

const handleBatchPay = async () => {
  if (selectedBills.value.length === 0) {
    ElMessage.warning('请选择要缴费的账单')
    return
  }
  
  const totalAmount = selectedBills.value.reduce((sum, item) => {
    const remaining = item.totalAmount - (item.paidAmount || 0)
    return sum + remaining
  }, 0)
  
  try {
    await ElMessageBox.confirm(`您选择了 ${selectedBills.value.length} 个账单，共计 ¥${totalAmount.toFixed(2)}，确认支付？`, '批量缴费确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const billIds = selectedBills.value.map(item => item.id)
    const res = await request.post('/api/fee-bill/batch-pay', billIds, {
      params: {
        paymentMethod: '微信支付'
      }
    })
    if (res.code === 200) {
      ElMessage.success('批量缴费成功')
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
.fee-page {
  .filter-card {
    margin-bottom: 20px;
  }
  
  .query-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      font-weight: 600;
    }
  }
  
  .amount,
  .total-amount {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .late-fee {
    color: #e6a23c;
  }
  
  .dialog-amount {
    color: #f56c6c;
    font-size: 18px;
    font-weight: 600;
  }
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>