<template>
  <div class="notice-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="标题">
          <el-input v-model="queryForm.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部类型" clearable>
            <el-option label="一般通知" value="GENERAL" />
            <el-option label="重要通知" value="IMPORTANT" />
            <el-option label="紧急通知" value="EMERGENCY" />
            <el-option label="活动通知" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="$router.push('/property/notice/add')">
            <el-icon><Plus /></el-icon>
            发布公告
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="title" label="公告标题" min-width="200">
          <template #default="{ row }">
            <div class="title-cell">
              <el-tag v-if="row.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
              <span class="title-text">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="viewCount" label="浏览次数" width="100">
          <template #default="{ row }">
            {{ row.viewCount || 0 }}次
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row.id)">
              详情
            </el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
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

const queryForm = reactive({
  title: '',
  type: '',
  status: null
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
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (queryForm.title) params.title = queryForm.title
    if (queryForm.type) params.type = queryForm.type
    if (queryForm.status !== null) params.status = queryForm.status
    
    const res = await request.get('/api/notice/page', { params })
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
  queryForm.title = ''
  queryForm.type = ''
  queryForm.status = null
  handleSearch()
}

const viewDetail = (id) => {
  router.push(`/property/notice/detail/${id}`)
}

const handleEdit = (id) => {
  router.push(`/property/notice/edit/${id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.delete(`/api/notice/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
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
.notice-page {
  .filter-card {
    margin-bottom: 20px;
  }
  
  .query-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }
  
  .title-cell {
    display: flex;
    align-items: center;
    
    .el-tag {
      margin-right: 8px;
    }
    
    .title-text {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>