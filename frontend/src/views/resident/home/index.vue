<template>
  <div class="resident-home">
    <div class="welcome-card">
      <div class="welcome-text">
        <h2>您好，{{ userInfo.realName || userInfo.username }}！</h2>
        <p>欢迎使用社区物业便民服务系统</p>
        <p class="current-time">{{ currentTime }}</p>
      </div>
      <div class="welcome-icon">
        <el-icon :size="80"><HomeFilled /></el-icon>
      </div>
    </div>

    <div class="quick-access">
      <div class="section-title">快捷入口</div>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="quick-item" @click="$router.push('/resident/repair')">
            <div class="quick-icon repair">
              <el-icon :size="32"><Tools /></el-icon>
            </div>
            <div class="quick-text">物业报修</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="quick-item" @click="$router.push('/resident/visitor')">
            <div class="quick-icon visitor">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="quick-text">访客预约</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="quick-item" @click="$router.push('/resident/fee')">
            <div class="quick-icon fee">
              <el-icon :size="32"><Wallet /></el-icon>
            </div>
            <div class="quick-text">费用缴纳</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="quick-item" @click="$router.push('/resident/notice')">
            <div class="quick-icon notice">
              <el-icon :size="32"><Bell /></el-icon>
            </div>
            <div class="quick-text">公告通知</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="data-card">
          <template #header>
            <span class="card-title">数据概览</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="data-item">
                <div class="data-value">{{ pendingRepairCount }}</div>
                <div class="data-label">待处理报修</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="data-item">
                <div class="data-value">{{ pendingFeeCount }}</div>
                <div class="data-label">待缴费账单</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="notice-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">最新公告</span>
              <el-link type="primary" @click="$router.push('/resident/notice')">查看更多</el-link>
            </div>
          </template>
          <div v-if="latestNotices.length > 0" class="notice-list">
            <div 
              v-for="notice in latestNotices" 
              :key="notice.id" 
              class="notice-item"
              @click="viewNotice(notice.id)"
            >
              <span class="notice-title">{{ notice.title }}</span>
              <span class="notice-time">{{ formatDate(notice.createTime) }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无公告" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const currentTime = ref('')
const pendingRepairCount = ref(0)
const pendingFeeCount = ref(0)
const latestNotices = ref([])

let timer = null

const updateTime = () => {
  currentTime.value = dayjs().format('YYYY年MM月DD日 HH:mm:ss dddd')
}

const loadData = async () => {
  try {
    const [repairRes, feeRes, noticeRes] = await Promise.all([
      request.get('/api/repair/page/my', { params: { status: 'PENDING' } }),
      request.get('/api/fee-bill/pending'),
      request.get('/api/notice/page/published', { params: { current: 1, size: 5 } })
    ])
    
    pendingRepairCount.value = repairRes.data?.total || 0
    pendingFeeCount.value = feeRes.data?.length || 0
    latestNotices.value = noticeRes.data?.records || []
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const formatDate = (date) => {
  return dayjs(date).format('MM-DD HH:mm')
}

const viewNotice = (id) => {
  router.push(`/resident/notice/detail/${id}`)
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadData()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.resident-home {
  padding: 0;
}

.welcome-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  color: #fff;
  
  h2 {
    margin: 0 0 10px 0;
    font-size: 24px;
  }
  
  p {
    margin: 0 0 5px 0;
    opacity: 0.9;
  }
  
  .current-time {
    font-size: 14px;
    opacity: 0.8;
  }
  
  .welcome-icon {
    opacity: 0.5;
  }
}

.quick-access {
  margin-bottom: 20px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 15px;
    color: #333;
  }
  
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30px 0;
    background: #fff;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    }
    
    .quick-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12px;
      
      &.repair {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: #fff;
      }
      
      &.visitor {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: #fff;
      }
      
      &.fee {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        color: #fff;
      }
      
      &.notice {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        color: #fff;
      }
    }
    
    .quick-text {
      font-size: 14px;
      color: #333;
    }
  }
}

.data-card,
.notice-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-item {
  text-align: center;
  padding: 20px 0;
  
  .data-value {
    font-size: 32px;
    font-weight: 700;
    color: #409EFF;
  }
  
  .data-label {
    font-size: 14px;
    color: #909399;
    margin-top: 5px;
  }
}

.notice-list {
  .notice-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: all 0.3s;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background: #f5f7fa;
      padding: 12px 10px;
      margin: 0 -10px;
    }
    
    .notice-title {
      font-size: 14px;
      color: #333;
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .notice-time {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>