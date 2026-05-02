<template>
  <div class="fee-detail-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="title">账单详情</span>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else class="detail-content">
        <div class="detail-header">
          <h3>{{ detail.billNo }}</h3>
          <el-tag :type="getStatusType(detail.status)" size="large">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="费用类型">{{ detail.feeTypeName }}</el-descriptions-item>
          <el-descriptions-item label="账单月份">{{ detail.billMonth || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房屋信息">
            {{ detail.buildingNo }}{{ detail.unitNo || '' }}{{ detail.roomNo || '' }}
          </el-descriptions-item>
          <el-descriptions-item label="应缴日期">{{ formatDate(detail.payableDate) }}</el-descriptions-item>
          <el-descriptions-item label="金额">
            <span class="amount">¥{{ detail.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="滞纳金">
            <span v-if="detail.lateFee > 0" class="late-fee">¥{{ detail.lateFee }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="应付总额">
            <span class="total-amount">¥{{ detail.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已付金额">
            <span>¥{{ detail.paidAmount || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ detail.remark || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.paidTime" class="payment-section">
          <h4>缴费信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="缴费时间">{{ formatDate(detail.paidTime) }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ detail.paymentMethod }}</el-descriptions-item>
            <el-descriptions-item label="交易流水号" :span="2">
              {{ detail.transactionNo || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="detail.status !== 'PAID'" class="action-section">
          <el-button type="primary" size="large" @click="handlePay">
            立即缴费 ¥{{ (detail.totalAmount - (detail.paidAmount || 0)).toFixed(2) }}
          </el-button>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="payDialogVisible" title="缴费确认" width="400px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="账单编号">{{ detail.billNo }}</el-descriptions-item>
        <el-descriptions-item label="应付金额">
          <span class="dialog-amount">¥{{ (detail.totalAmount - (detail.paidAmount || 0)).toFixed(2) }}</span>
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const detail = ref({})
const payDialogVisible = ref(false)
const payMethod = ref('微信支付')
const payLoading = ref(false)

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

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/fee-bill/${route.params.id}`)
    if (res.code === 200) {
      detail.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const handlePay = () => {
  payDialogVisible.value = true
}

const confirmPay = async () => {
  payLoading.value = true
  try {
    const res = await request.post(`/api/fee-bill/pay/${route.params.id}`, null, {
      params: {
        paymentMethod: payMethod.value
      }
    })
    if (res.code === 200) {
      ElMessage.success('缴费成功')
      payDialogVisible.value = false
      loadDetail()
    }
  } finally {
    payLoading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.fee-detail-page {
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
    
    .amount,
    .total-amount {
      color: #f56c6c;
      font-weight: 600;
      font-size: 16px;
    }
    
    .late-fee {
      color: #e6a23c;
    }
    
    .payment-section,
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
  
  .dialog-amount {
    color: #f56c6c;
    font-size: 20px;
    font-weight: 700;
  }
}
</style>