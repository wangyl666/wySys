import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/resident',
    name: 'ResidentLayout',
    component: () => import('@/layouts/ResidentLayout.vue'),
    redirect: '/resident/home',
    meta: { requiresAuth: true, role: 'RESIDENT' },
    children: [
      {
        path: 'home',
        name: 'ResidentHome',
        component: () => import('@/views/resident/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'repair',
        name: 'ResidentRepair',
        component: () => import('@/views/resident/repair/index.vue'),
        meta: { title: '物业报修' }
      },
      {
        path: 'repair/add',
        name: 'ResidentRepairAdd',
        component: () => import('@/views/resident/repair/add.vue'),
        meta: { title: '提交报修' }
      },
      {
        path: 'repair/detail/:id',
        name: 'ResidentRepairDetail',
        component: () => import('@/views/resident/repair/detail.vue'),
        meta: { title: '报修详情' }
      },
      {
        path: 'visitor',
        name: 'ResidentVisitor',
        component: () => import('@/views/resident/visitor/index.vue'),
        meta: { title: '访客预约' }
      },
      {
        path: 'visitor/add',
        name: 'ResidentVisitorAdd',
        component: () => import('@/views/resident/visitor/add.vue'),
        meta: { title: '预约访客' }
      },
      {
        path: 'visitor/detail/:id',
        name: 'ResidentVisitorDetail',
        component: () => import('@/views/resident/visitor/detail.vue'),
        meta: { title: '预约详情' }
      },
      {
        path: 'fee',
        name: 'ResidentFee',
        component: () => import('@/views/resident/fee/index.vue'),
        meta: { title: '费用缴纳' }
      },
      {
        path: 'fee/detail/:id',
        name: 'ResidentFeeDetail',
        component: () => import('@/views/resident/fee/detail.vue'),
        meta: { title: '账单详情' }
      },
      {
        path: 'notice',
        name: 'ResidentNotice',
        component: () => import('@/views/resident/notice/index.vue'),
        meta: { title: '公告通知' }
      },
      {
        path: 'notice/detail/:id',
        name: 'ResidentNoticeDetail',
        component: () => import('@/views/resident/notice/detail.vue'),
        meta: { title: '公告详情' }
      },
      {
        path: 'profile',
        name: 'ResidentProfile',
        component: () => import('@/views/resident/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/property',
    name: 'PropertyLayout',
    component: () => import('@/layouts/PropertyLayout.vue'),
    redirect: '/property/home',
    meta: { requiresAuth: true, role: 'PROPERTY' },
    children: [
      {
        path: 'home',
        name: 'PropertyHome',
        component: () => import('@/views/property/home/index.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'user',
        name: 'PropertyUser',
        component: () => import('@/views/property/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'building',
        name: 'PropertyBuilding',
        component: () => import('@/views/property/building/index.vue'),
        meta: { title: '楼栋管理' }
      },
      {
        path: 'house',
        name: 'PropertyHouse',
        component: () => import('@/views/property/house/index.vue'),
        meta: { title: '房屋管理' }
      },
      {
        path: 'repair',
        name: 'PropertyRepair',
        component: () => import('@/views/property/repair/index.vue'),
        meta: { title: '报修管理' }
      },
      {
        path: 'repair/detail/:id',
        name: 'PropertyRepairDetail',
        component: () => import('@/views/property/repair/detail.vue'),
        meta: { title: '报修详情' }
      },
      {
        path: 'visitor',
        name: 'PropertyVisitor',
        component: () => import('@/views/property/visitor/index.vue'),
        meta: { title: '访客管理' }
      },
      {
        path: 'visitor/detail/:id',
        name: 'PropertyVisitorDetail',
        component: () => import('@/views/property/visitor/detail.vue'),
        meta: { title: '访客详情' }
      },
      {
        path: 'fee-type',
        name: 'PropertyFeeType',
        component: () => import('@/views/property/fee-type/index.vue'),
        meta: { title: '费用类型' }
      },
      {
        path: 'fee-bill',
        name: 'PropertyFeeBill',
        component: () => import('@/views/property/fee-bill/index.vue'),
        meta: { title: '账单管理' }
      },
      {
        path: 'fee-bill/add',
        name: 'PropertyFeeBillAdd',
        component: () => import('@/views/property/fee-bill/add.vue'),
        meta: { title: '创建账单' }
      },
      {
        path: 'payment-record',
        name: 'PropertyPaymentRecord',
        component: () => import('@/views/property/payment-record/index.vue'),
        meta: { title: '缴费记录' }
      },
      {
        path: 'notice',
        name: 'PropertyNotice',
        component: () => import('@/views/property/notice/index.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'notice/add',
        name: 'PropertyNoticeAdd',
        component: () => import('@/views/property/notice/add.vue'),
        meta: { title: '发布公告' }
      },
      {
        path: 'notice/edit/:id',
        name: 'PropertyNoticeEdit',
        component: () => import('@/views/property/notice/edit.vue'),
        meta: { title: '编辑公告' }
      },
      {
        path: 'notice/detail/:id',
        name: 'PropertyNoticeDetail',
        component: () => import('@/views/property/notice/detail.vue'),
        meta: { title: '公告详情' }
      },
      {
        path: 'profile',
        name: 'PropertyProfile',
        component: () => import('@/views/property/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 社区物业便民服务系统` : '社区物业便民服务系统'
  
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiredRole = to.matched.find(record => record.meta.role)?.meta.role
  
  if (requiresAuth) {
    if (!token) {
      if (to.path === '/login') {
        next()
        return
      }
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    
    if (requiredRole) {
      let isAllowed = false
      if (requiredRole === 'RESIDENT') {
        isAllowed = userRole === 'RESIDENT'
      } else if (requiredRole === 'PROPERTY') {
        isAllowed = userRole === 'PROPERTY' || userRole === 'ADMIN'
      }
      
      if (!isAllowed) {
        if (userRole === 'RESIDENT') {
          if (to.path === '/resident/home' || to.path === '/resident') {
            next()
            return
          }
          next('/resident/home')
        } else if (userRole === 'PROPERTY' || userRole === 'ADMIN') {
          if (to.path === '/property/home' || to.path === '/property') {
            next()
            return
          }
          next('/property/home')
        } else {
          localStorage.removeItem('token')
          localStorage.removeItem('userRole')
          localStorage.removeItem('userInfo')
          next('/login')
        }
        return
      }
    }
    
    if (!userStore.userInfo || !userStore.userInfo.id) {
      try {
        await userStore.getCurrentUser()
      } catch (error) {
        localStorage.removeItem('token')
        localStorage.removeItem('userRole')
        localStorage.removeItem('userInfo')
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
    }
  }
  
  if (to.path === '/login' && token) {
    if (userRole === 'RESIDENT') {
      next('/resident/home')
    } else if (userRole === 'PROPERTY' || userRole === 'ADMIN') {
      next('/property/home')
    } else {
      next()
    }
    return
  }
  
  if (to.path === '/') {
    if (token) {
      if (userRole === 'RESIDENT') {
        next('/resident/home')
      } else if (userRole === 'PROPERTY' || userRole === 'ADMIN') {
        next('/property/home')
      } else {
        next('/login')
      }
    } else {
      next('/login')
    }
    return
  }
  
  next()
})

export default router