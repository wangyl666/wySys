<template>
  <div class="notice-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部类型" clearable>
            <el-option label="一般通知" value="GENERAL" />
            <el-option label="重要通知" value="IMPORTANT" />
            <el-option label="紧急通知" value="EMERGENCY" />
            <el-option label="活动通知" value="ACTIVITY" />
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

    <el-card class="list-card">
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="noticeList.length === 0" class="empty-container">
        <el-empty description="暂无公告" />
      </div>

      <div v-else class="notice-list">
        <div 
          v-for="notice in noticeList" 
          :key="notice.id" 
          class="notice-item"
          @click="viewDetail(notice.id)"
        >
          <div class="notice-left">
            <div class="notice-badge">
              <el-tag v-if="notice.isTop" type="danger" size="small">置顶</el-tag>
              <el-tag v-else :type="getTypeTagType(notice.type)" size="small">
                {{ getTypeText(notice.type) }}
              </el-tag>
            </div>
            <h3 class="notice-title">{{ notice.title }}</h3>
            <p class="notice-summary">{{ truncate(notice.content, 100) }}</p>
            <div class="notice-meta">
              <span>发布人：{{ notice.publisherName }}</span>
              <span>发布时间：{{ formatDate(notice.createTime) }}</span>
              <span>浏览：{{ notice.viewCount || 0 }}次</span>
            </div>
          </div>
          <div class="notice-right">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
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
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const noticeList = ref([])

const queryForm = reactive({
  type: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const typeMap = {
  GENERAL: { text: '一般通知', tagType: '' },
  IMPORTANT: { text: '重要通知', tagType: 'warning' },
  EMERGENCY: { text: '紧急通知', tagType: 'danger' },
  ACTIVITY: { text: '活动通知', tagType: 'success' }
}

const getTypeText = (type) => typeMap[type]?.text || type
const getTypeTagType = (type) => typeMap[type]?.tagType || ''

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const truncate = (text, length) => {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (queryForm.type) params.type = queryForm.type
    
    const res = await request.get('/api/notice/page/published', { params })
    if (res.code === 200) {
      noticeList.value = res.data.records
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
  queryForm.type = ''
  handleSearch()
}

const viewDetail = (id) => {
  router.push(`/resident/notice/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.notice-page {
  .filter-card {
    margin-bottom: 20px;
  }
  
  .query-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }
  
  .loading-container,
  .empty-container {
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
  
  .notice-list {
    .notice-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.3s;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        background: #f5f7fa;
        margin: 0 -20px;
        padding: 20px 20px;
      }
      
      .notice-left {
        flex: 1;
        
        .notice-badge {
          margin-bottom: 10px;
        }
        
        .notice-title {
          margin: 0 0 10px 0;
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }
        
        .notice-summary {
          margin: 0 0 10px 0;
          font-size: 14px;
          color: #666;
          line-height: 1.6;
        }
        
        .notice-meta {
          display: flex;
          gap: 20px;
          font-size: 12px;
          color: #999;
        }
      }
      
      .notice-right {
        color: #c0c4cc;
      }
    }
  }
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>