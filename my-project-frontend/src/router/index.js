import { createRouter, createWebHistory } from 'vue-router'
import { unauthorized } from "@/net";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }, {
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
            children:[
                {
                    path: 'user-setting',
                    name: 'user-setting',
                    component:()=>import('@/views/settings/UserSettings.vue')
                },
                {
                    path:'privacy-setting',
                    name: 'privacy-setting',
                    component:()=>import('@/views/settings/PrivacySetting.vue')
                }
            ]
        }
    ]
})

//路由守卫
router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    if(to.name.startsWith('welcome') && !isUnauthorized) {//如果用户已经登录，不能再访问登录页面
        next('/index')
    } else if(to.fullPath.startsWith('/index') && isUnauthorized) {
        next('/')//r如果没有登录就访问index主页，则重定向到登录页面
    } else {
        next()
    }
})

export default router
