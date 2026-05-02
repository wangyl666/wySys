<template>
  <div class="repair-detail-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="title">报修详情</span>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else class="detail-content">
        <div class="detail-header">
          <h3>{{ detail.title }}</h3>
          <el-tag :type="getStatusType(detail.status)" size="large">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="报修类型">{{ detail.type }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="维修地址">{{ detail.address }}</el-descriptions-item>
          <el-descriptions-item label="期望时间">{{ formatDate(detail.preferredTime) || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="业主">{{ detail.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="业主电话">{{ detail.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报修内容" :span="2">
            {{ detail.content }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.images && detail.images.length > 0" class="image-section">
          <h4>报修图片</h4>
          <div class="image-list">
            <el-image
              v-for="(img, index) in detail.images"
              :key="index"
              :src="img"
              :preview-src-list="detail.images"
              fit="cover"
              class="preview-image"
            />
          </div>
        </div>

        <div v-if="detail.staffName" class="process-section">
          <h4>处理信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="处理人员">{{ detail.staffName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.staffPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理内容" :span="2">
              {{ detail.processContent || '暂无处理内容' }}
            </el-descriptions-item>
            <el-descriptions-item label="处理时间" :span="2">
              {{ formatDate(detail.processTime) || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="detail.rating" class="comment-section">
          <h4>业主评价</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="评分">
              <el-rate v-model="detail.rating" disabled />
            </el-descriptions-item>
            <el-descriptions-item label="评价时间">{{ formatDate(detail.commentTime) }}</el-descriptions-item>
            <el-descriptions-item label="评价内容" :span="2">
              {{ detail.comment || '暂无评价' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="action-section">
          <el-button 
            type="success" 
            @click="handleAssign"
            v-if="detail.status === 'PENDING'"
          >
            派单
          </el-button>
          <el-button 
            type="primary" 
            @click="handleComplete"
            v-if="detail.status === 'PROCESSING'"
          >
            完成
          </el-button>
        </div>
      </div>
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
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const detail = ref({})
const assignDialogVisible = ref(false)
const assigning = ref(false)
const assignFormRef = ref(null)

const staffList = ref([])
const assignForm = reactive({
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
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/repair/${route.params.id}`)
    if (res.code === 200) {
      detail.value = res.data
      if (detail.value.images) {
        try {
          detail.value.images = JSON.parse(detail.value.images)
        } catch (e) {
          detail.value.images = []
        }
      }
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

const handleAssign = () => {
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
    const res = await request.put(`/api/repair/assign/${route.params.id}`, null, {
      params: {
        staffId: assignForm.staffId,
        staffName: assignForm.staffName,
        staffPhone: assignForm.staffPhone
      }
    })
    if (res.code === 200) {
      ElMessage.success('派单成功')
      assignDialogVisible.value = false
      loadDetail()
    }
  } finally {
    assigning.value = false
  }
}

const handleComplete = async () => {
  try {
    await ElMessageBox.confirm('确定要完成该报修吗？完成后将无法撤销。', '提示', {
      confirmButtonText: '确定完成',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.put(`/api/repair/complete/${route.params.id}`)
    if (res.code === 200) {
      ElMessage.success('操作成功')
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
  loadStaffList()
})
</script>

<style lang="scss" scoped>
.repair-detail-page {
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
    
    .image-section,
    .process-section,
    .comment-section,
    .action-section {
      margin-top: 20px;
      
      h4 {
        margin: 0 0 15px 0;
        font-size: 16px;
        padding-bottom: 10px;
        border-bottom: 1px solid #f0f0f0;
      }
      
      .image-list {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
      }
      
      .preview-image {
        width: 120px;
        height: 120px;
        border-radius: 4px;
        cursor: pointer;
      }
    }
  }
}
</style>