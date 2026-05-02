<template>
  <div class="fee-bill-add-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="title">创建账单</span>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
        class="add-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="费用类型" prop="feeTypeId">
              <el-select 
                v-model="form.feeTypeId" 
                placeholder="请选择费用类型" 
                style="width: 100%"
                @change="handleFeeTypeChange"
              >
                <el-option 
                  v-for="item in feeTypeList" 
                  :key="item.id" 
                  :label="item.typeName" 
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用类型名称">
              <el-input v-model="form.feeTypeName" placeholder="自动填充" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业主" prop="userId">
              <el-select 
                v-model="form.userId" 
                placeholder="请选择业主" 
                filterable
                style="width: 100%"
                @change="handleUserChange"
              >
                <el-option 
                  v-for="item in userList" 
                  :key="item.id" 
                  :label="`${item.realName || item.username} - ${item.phone}`" 
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房屋" prop="houseId">
              <el-select 
                v-model="form.houseId" 
                placeholder="请选择房屋" 
                style="width: 100%"
              >
                <el-option 
                  v-for="item in houseList" 
                  :key="item.id" 
                  :label="`${item.buildingNo}${item.unitNo || ''}${item.roomNo || ''}`" 
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账单月份" prop="billMonth">
              <el-date-picker
                v-model="form.billMonth"
                type="month"
                placeholder="选择月份"
                value-format="yyyy-MM"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="应缴日期" prop="payableDate">
              <el-date-picker
                v-model="form.payableDate"
                type="date"
                placeholder="选择应缴日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计费数量" prop="quantity">
              <el-input-number 
                v-model="form.quantity" 
                :min="0" 
                :precision="2"
                placeholder="请输入计费数量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价" prop="unitPrice">
              <el-input-number 
                v-model="form.unitPrice" 
                :min="0" 
                :precision="2"
                placeholder="请输入单价"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number 
                v-model="form.amount" 
                :min="0" 
                :precision="2"
                placeholder="请输入金额"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="滞纳金">
              <el-input-number 
                v-model="form.lateFee" 
                :min="0" 
                :precision="2"
                placeholder="请输入滞纳金（选填）"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="应付总额">
          <el-input-number 
            v-model="form.totalAmount" 
            :min="0" 
            :precision="2"
            placeholder="自动计算：金额 + 滞纳金"
            style="width: 100%"
            disabled
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注（选填）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">创建账单</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const feeTypeList = ref([])
const userList = ref([])
const houseList = ref([])

const form = reactive({
  feeTypeId: null,
  feeTypeName: '',
  userId: null,
  houseId: null,
  billMonth: '',
  payableDate: '',
  quantity: null,
  unitPrice: null,
  amount: null,
  lateFee: 0,
  totalAmount: null,
  remark: ''
})

const rules = {
  feeTypeId: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
  userId: [{ required: true, message: '请选择业主', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

watch(() => [form.amount, form.lateFee], () => {
  const amount = form.amount || 0
  const lateFee = form.lateFee || 0
  form.totalAmount = amount + lateFee
}, { deep: true })

const loadFeeTypeList = async () => {
  try {
    const res = await request.get('/api/fee-type/list')
    if (res.code === 200) {
      feeTypeList.value = res.data
    }
  } catch (error) {
    console.error('加载费用类型失败:', error)
  }
}

const loadUserList = async () => {
  try {
    const res = await request.get('/api/user/page', {
      params: { role: 'RESIDENT', size: 100 }
    })
    if (res.code === 200) {
      userList.value = res.data.records
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const handleFeeTypeChange = (feeTypeId) => {
  const feeType = feeTypeList.value.find(item => item.id === feeTypeId)
  if (feeType) {
    form.feeTypeName = feeType.typeName
    if (feeType.price) {
      form.unitPrice = feeType.price
    }
  }
}

const handleUserChange = async (userId) => {
  form.houseId = null
  houseList.value = []
  
  try {
    const res = await request.get(`/api/house/my`, {
      params: { userId }
    })
    if (res.code === 200) {
      houseList.value = res.data
    }
  } catch (error) {
    console.error('加载房屋列表失败:', error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const data = {
      ...form,
      totalAmount: form.amount + (form.lateFee || 0)
    }
    
    const res = await request.post('/api/fee-bill', data)
    if (res.code === 200) {
      ElMessage.success('账单创建成功')
      router.push('/property/fee-bill')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFeeTypeList()
  loadUserList()
})
</script>

<style lang="scss" scoped>
.fee-bill-add-page {
  .card-header {
    display: flex;
    align-items: center;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      margin-left: 10px;
    }
  }
  
  .add-form {
    max-width: 800px;
    padding: 20px 0;
  }
}
</style>