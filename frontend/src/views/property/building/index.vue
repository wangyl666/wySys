<template>
  <div class="building-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="楼栋号">
          <el-input v-model="queryForm.buildingNo" placeholder="楼栋号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item v-if="userStore.isAdmin">
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增楼栋
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="buildingNo" label="楼栋号" width="100" />
        <el-table-column prop="buildingName" label="楼栋名称" width="120">
          <template #default="{ row }">
            {{ row.buildingName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalFloors" label="总层数" width="80">
          <template #default="{ row }">
            {{ row.totalFloors || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="unitsPerFloor" label="每层单元数" width="100">
          <template #default="{ row }">
            {{ row.unitsPerFloor || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalHouses" label="总房屋数" width="100">
          <template #default="{ row }">
            {{ row.totalHouses || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="负责人员" min-width="150">
          <template #default="{ row }">
            <template v-if="row.staffList && row.staffList.length > 0">
              <el-tag v-for="staff in row.staffList" :key="staff.id" size="small" style="margin-right: 5px; margin-bottom: 5px;">
                {{ staff.realName }}
              </el-tag>
            </template>
            <span v-else class="text-gray">暂未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleAssign(row)" v-if="userStore.isAdmin">
              分配人员
            </el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)" v-if="userStore.isAdmin">
              编辑
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'danger' : 'success'" 
              link 
              size="small"
              @click="handleToggleStatus(row)"
              v-if="userStore.isAdmin"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="楼栋号" prop="buildingNo">
          <el-input v-model="form.buildingNo" placeholder="如：1号楼、A栋" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="楼栋名称">
          <el-input v-model="form.buildingName" placeholder="楼栋名称（可选）" />
        </el-form-item>
        <el-form-item label="总层数">
          <el-input-number v-model="form.totalFloors" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="每层单元数">
          <el-input-number v-model="form.unitsPerFloor" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="总房屋数">
          <el-input-number v-model="form.totalHouses" :min="0" :max="1000" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="楼栋描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配物业人员" width="500px">
      <el-form label-width="100px">
        <el-form-item label="楼栋">
          <el-tag>{{ currentBuilding.buildingNo }}</el-tag>
        </el-form-item>
        <el-form-item label="物业人员">
          <el-transfer
            v-model="selectedStaffIds"
            :data="propertyStaffList"
            :props="{
              key: 'id',
              label: 'realName'
            }"
            filterable
            filter-placeholder="搜索物业人员"
            titles="['可选人员', '已选人员']"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSubmitting" @click="handleSaveAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const assignDialogVisible = ref(false)
const submitting = ref(false)
const assignSubmitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentBuilding = ref({})
const selectedStaffIds = ref([])
const propertyStaffList = ref([])

const queryForm = reactive({
  buildingNo: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  buildingNo: '',
  buildingName: '',
  totalFloors: 1,
  unitsPerFloor: 1,
  totalHouses: 0,
  description: ''
})

const rules = {
  buildingNo: [{ required: true, message: '请输入楼栋号', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑楼栋' : '新增楼栋')

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (queryForm.buildingNo) params.buildingNo = queryForm.buildingNo
    if (queryForm.status !== null) params.status = queryForm.status
    
    const res = await request.get('/api/building/page', { params })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const loadPropertyStaffList = async () => {
  try {
    const res = await request.get('/api/user/page', {
      params: {
        current: 1,
        size: 1000,
        role: 'PROPERTY',
        status: 1
      }
    })
    if (res.code === 200) {
      propertyStaffList.value = res.data.records.map(item => ({
        ...item,
        realName: item.realName || item.username
      }))
    }
  } catch (error) {
    console.error('加载物业人员列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  queryForm.buildingNo = ''
  queryForm.status = null
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.buildingNo = ''
  form.buildingName = ''
  form.totalFloors = 1
  form.unitsPerFloor = 1
  form.totalHouses = 0
  form.description = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.buildingNo = row.buildingNo
  form.buildingName = row.buildingName || ''
  form.totalFloors = row.totalFloors || 1
  form.unitsPerFloor = row.unitsPerFloor || 1
  form.totalHouses = row.totalHouses || 0
  form.description = row.description || ''
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const data = {
      buildingNo: form.buildingNo,
      buildingName: form.buildingName,
      totalFloors: form.totalFloors,
      unitsPerFloor: form.unitsPerFloor,
      totalHouses: form.totalHouses,
      description: form.description,
      status: 1
    }
    
    let res
    if (isEdit.value) {
      data.id = form.id
      res = await request.put('/api/building', data)
    } else {
      res = await request.post('/api/building', data)
    }
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    }
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该楼栋吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.put(`/api/building/status/${row.id}`, null, {
      params: { status: row.status === 1 ? 0 : 1 }
    })
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleAssign = async (row) => {
  currentBuilding.value = row
  selectedStaffIds.value = row.staffIds ? [...row.staffIds] : []
  await loadPropertyStaffList()
  assignDialogVisible.value = true
}

const handleSaveAssign = async () => {
  assignSubmitting.value = true
  try {
    const res = await request.post(`/api/building/assign/${currentBuilding.value.id}`, selectedStaffIds.value)
    if (res.code === 200) {
      ElMessage.success('分配成功')
      assignDialogVisible.value = false
      loadData()
    }
  } finally {
    assignSubmitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.building-page {
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
  
  .text-gray {
    color: #909399;
  }
}
</style>
