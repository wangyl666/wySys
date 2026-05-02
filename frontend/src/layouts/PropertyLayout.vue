<template>
  <el-container class="property-layout">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <el-icon size="28"><OfficeBuilding /></el-icon>
        <span>物业管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        class="layout-menu"
      >
        <el-menu-item index="/property/home">
          <el-icon><DataAnalysis /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/property/building" v-if="userStore.isAdmin">
          <el-icon><OfficeBuilding /></el-icon>
          <span>楼栋管理</span>
        </el-menu-item>
        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/property/user">用户列表</el-menu-item>
          <el-menu-item index="/property/house">房屋管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/property/repair">
          <el-icon><Tools /></el-icon>
          <span>报修管理</span>
        </el-menu-item>
        <el-menu-item index="/property/visitor">
          <el-icon><UserFilled /></el-icon>
          <span>访客管理</span>
        </el-menu-item>
        <el-sub-menu index="fee">
          <template #title>
            <el-icon><Wallet /></el-icon>
            <span>费用管理</span>
          </template>
          <el-menu-item index="/property/fee-type">费用类型</el-menu-item>
          <el-menu-item index="/property/fee-bill">账单管理</el-menu-item>
          <el-menu-item index="/property/payment-record">缴费记录</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/property/notice">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/property/profile">
          <el-icon><UserFilled /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <span class="welcome">欢迎回来，{{ userInfo.realName || userInfo.username }}</span>
          <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
            {{ userInfo.role === 'ADMIN' ? '管理员' : '物业人员' }}
          </el-tag>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span>{{ userInfo.realName || userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/property/profile')
  } else if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style lang="scss" scoped>
.property-layout {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  
  span {
    margin-left: 10px;
  }
}

.layout-menu {
  border-right: none;
  
  :deep(.el-menu-item) {
    height: 50px;
    line-height: 50px;
  }
  
  :deep(.el-sub-menu__title) {
    height: 50px;
    line-height: 50px;
  }
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  
  .welcome {
    margin-right: 10px;
    font-size: 16px;
  }
}

.header-right {
  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    
    span {
      margin: 0 8px;
    }
  }
}

.layout-main {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>