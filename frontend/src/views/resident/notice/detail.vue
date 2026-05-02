<template>
  <div class="notice-detail-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="title">公告详情</span>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else class="detail-content">
        <div class="notice-header">
          <h2>{{ detail.title }}</h2>
          <div class="notice-meta">
            <span class="type">
              <el-tag :type="getTypeTagType(detail.type)" size="small">
                {{ getTypeText(detail.type) }}
              </el-tag>
            </span>
            <span class="publish" v-if="detail.isTop">
              <el-tag type="danger" size="small">置顶</el-tag>
            </span>
            <span>发布人：{{ detail.publisherName }}</span>
            <span>发布时间：{{ formatDate(detail.createTime) }}</span>
            <span>浏览：{{ detail.viewCount || 0 }}次</span>
          </div>
        </div>

        <el-divider />

        <div class="notice-content">
          {{ detail.content }}
        </div>

        <div v-if="detail.startTime || detail.endTime" class="notice-time">
          <el-divider />
          <p class="time-info">
            <span v-if="detail.startTime">显示开始时间：{{ formatDate(detail.startTime) }}</span>
            <span v-if="detail.endTime">显示结束时间：{{ formatDate(detail.endTime) }}</span>
          </p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()

const loading = ref(true)
const detail = ref({})

const typeMap = {
  GENERAL: { text: '一般通知', tagType: '' },
  IMPORTANT: { text: '重要通知', tagType: 'warning' },
  EMERGENCY: { text: '紧急通知', tagType: 'danger' },
  ACTIVITY: { text: '活动通知', tagType: 'success' }
}

const getTypeText = (type) => typeMap[type]?.text || type
const getTypeTagType = (type) => typeMap[type]?.tagType || ''

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/notice/${route.params.id}`)
    if (res.code === 200) {
      detail.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.notice-detail-page {
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
    .notice-header {
      text-align: center;
      
      h2 {
        margin: 0 0 20px 0;
        font-size: 24px;
        font-weight: 600;
        color: #333;
      }
      
      .notice-meta {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 20px;
        flex-wrap: wrap;
        font-size: 14px;
        color: #909399;
        
        .type,
        .publish {
          margin-right: 5px;
        }
      }
    }
    
    .notice-content {
      padding: 20px 0;
      font-size: 16px;
      line-height: 2;
      color: #333;
      white-space: pre-wrap;
    }
    
    .notice-time {
      .time-info {
        display: flex;
        gap: 30px;
        font-size: 14px;
        color: #909399;
        justify-content: flex-end;
      }
    }
  }
}
</style>