<template>
  <div class="fee-bill-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="账单月份">
          <el-date-picker
            v-model="queryForm.billMonth"
            type="month"
            placeholder="选择月份"
            value-format="yyyy-MM"
            clearable
          />
        </el-form-item>
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
          <el-button type="success" @click="$router.push('/property/fee-bill/add')">
            <el-icon><Plus /></el-icon>
            创建账单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="billNo" label="账单编号" width="180" />
        <el-table-column prop="feeTypeName" label="费用类型" width="120" />
        <el-table-column prop="billMonth" label="账单月份" width="100" />
        <el-table-column label="房屋信息" width="150">
          <template #default="{ row }">
            {{ row.buildingNo }}{{ row.unitNo || '' }}{{ row.roomNo || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="业主" width="100" />
        <el-table-column prop="amount" label="金额" width="100">
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
        <el-table-column prop="totalAmount" label="应付总额" width="110">
          <template #default="{ row }">
            <span class="total-amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payableDate" label="应缴日期" width="110">
          <template #default="{ row }">
            {{ formatDate(row.payableDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">详情</el-button>
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
import request from '@/utils/request'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])

const queryForm = reactive({
  billMonth: '',
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
  return date ? dayjs(date).format('YYYY-MM-DD') : '-'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (queryForm.billMonth) params.billMonth = queryForm.billMonth
    if (queryForm.status) params.status = queryForm.status
    
    const res = await request.get('/api/fee-bill/page', { params })
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
  queryForm.billMonth = ''
  queryForm.status = ''
  handleSearch()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.fee-bill-page {
  .filter-card {
    margin-bottom: 20px;
  }
  
  .query-form {
    .el-form-item {
      margin-bottom: 0;
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
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>