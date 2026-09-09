<script setup>

import {Back, Message, Operation} from "@element-plus/icons-vue";
import {useStore} from "@/store";
import {logout} from "@/net";
import router from "@/router";

const store = useStore()
function userLogout(){
  logout(()=> router.push("/"))
}
</script>

<template>
  <div class="user-info">
    <slot/>
    <div class="profile">
      <div>{{store.user.username}}</div>
      <div>{{store.user.email}}</div>
    </div>
    <el-dropdown>
      <el-avatar  :src="store.avatarUrl"/>
      <template #dropdown>
        <el-dropdown-item>
          <el-icon><Operation/></el-icon>
          个人设置
        </el-dropdown-item>
        <el-dropdown-item>
          <el-icon><Message/></el-icon>
          消息列表
        </el-dropdown-item>
        <el-dropdown-item @click="userLogout" divided >
          <el-icon><Back/></el-icon>
          <span style="color: #f1908c">退出登录</span>
        </el-dropdown-item>
      </template>
    </el-dropdown>
  </div>
</template>

<style scoped>
.user-info{
  display: flex;
  justify-content: flex-end;
  align-items: center;

  .el-avatar:hover{
    cursor: pointer;
  }

  .profile{
    text-align: right;
    margin-right: 20px ;

    :first-child{
      font-size: 18px;
      font-weight: bold;
      line-height: 20px;
    }
    :last-child{
      font-size: 10px;
      color: grey;
    }
  }
}
</style>