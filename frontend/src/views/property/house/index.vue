<template>
  <div class="house-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="楼栋号">
          <el-input v-model="queryForm.buildingNo" placeholder="楼栋号" clearable />
        </el-form-item>
        <el-form-item label="单元号">
          <el-input v-model="queryForm.unitNo" placeholder="单元号" clearable />
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
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="buildingNo" label="楼栋号" width="100" />
        <el-table-column prop="unitNo" label="单元号" width="100">
          <template #default="{ row }">
            {{ row.unitNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="area" label="面积(㎡)" width="100">
          <template #default="{ row }">
            {{ row.area || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="房屋类型" width="100">
          <template #default="{ row }">
            {{ row.type || '-' }}
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
            <el-button type="primary" link size="small">编辑</el-button>
            <el-button type="danger" link size="small">删除</el-button>
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

const loading = ref(false)
const tableData = ref([])

const queryForm = reactive({
  buildingNo: '',
  unitNo: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const statusMap = {
  0: { text: '空置', type: 'info' },
  1: { text: '已入住', type: 'success' },
  2: { text: '已出租', type: 'warning' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (queryForm.buildingNo) params.buildingNo = queryForm.buildingNo
    if (queryForm.unitNo) params.unitNo = queryForm.unitNo
    
    const res = await request.get('/api/house/page', { params })
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
  queryForm.buildingNo = ''
  queryForm.unitNo = ''
  handleSearch()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.house-page {
  .filter-card {
    margin-bottom: 20px;
  }
  
  .query-form {
    .el-form-item {
      margin-bottom: 0;
    }
  }
  
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>