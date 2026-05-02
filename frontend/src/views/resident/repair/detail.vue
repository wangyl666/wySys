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
          <el-descriptions-item label="期望维修时间">{{ formatDate(detail.preferredTime) || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
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
          <h4>评价信息</h4>
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

        <div v-if="detail.status === 'COMPLETED' && !detail.rating" class="action-section">
          <h4>进行评价</h4>
          <el-form label-width="80px" class="rating-form">
            <el-form-item label="评分">
              <el-rate v-model="ratingForm.rating" show-text :texts="['极差', '失望', '一般', '满意', '惊喜']" />
            </el-form-item>
            <el-form-item label="评价">
              <el-input
                v-model="ratingForm.comment"
                type="textarea"
                :rows="3"
                placeholder="请输入评价内容（选填）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmitRating">
                提交评价
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="detail.status === 'PENDING' || detail.status === 'PROCESSING'" class="action-section">
          <el-button type="danger" @click="handleCancel">取消报修</el-button>
        </div>
      </div>
    </el-card>
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
const submitting = ref(false)
const detail = ref({})

const ratingForm = reactive({
  rating: 5,
  comment: ''
})

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

const handleSubmitRating = async () => {
  submitting.value = true
  try {
    const res = await request.put(`/api/repair/rate/${route.params.id}`, null, {
      params: {
        rating: ratingForm.rating,
        comment: ratingForm.comment
      }
    })
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      loadDetail()
    }
  } finally {
    submitting.value = false
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该报修申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.put(`/api/repair/cancel/${route.params.id}`)
    if (res.code === 200) {
      ElMessage.success('取消成功')
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
      
      .rating-form {
        max-width: 500px;
      }
    }
  }
}
</style>