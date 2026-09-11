<script setup>

import Card from "@/components/Card.vue";
import {Lock, Setting, Switch} from "@element-plus/icons-vue";
import {onMounted, reactive, ref} from "vue";
import {get,post} from "@/net/index.js";
import {ElMessage} from "element-plus";
import {apiUserChangePassword, apiUserPrivacy, apiUserPrivacySave} from "@/net/api/user";

const form = reactive({
  password: '',
  new_password: '',
  new_password_repeat: ''
})

const validatePassword = (rule,value,callback) =>{
  if(value === ''){
    callback(new Error('请再次输入密码'))
  }else if(value !== form.new_password){
    callback(new Error('两次输入的密码不一致'))
  }else {
    callback()
  }
}

const rules = {
  password:[
    {required:true, message:'请输入当前密码', trigger: ['blur']}
  ],
  new_password:[
    {required:true, message:'请输入新密码', trigger: ['blur']},
    {min:6, max: 16, message: '长度在6到16个字符', trigger: ['blur']}
  ],
  new_password_repeat:[
    {required:true, message:'请输入新密码', trigger: ['blur']},
    {validator: validatePassword, trigger: ['blur','change']}
  ]
}

const formRef = ref()
const valid = ref(false)
const onValidate=(prop,isValid) => valid.value= isValid

function resetPassword(){
  formRef.value.validate(valid =>{
    if(valid){
      apiUserChangePassword(form,() => {
        ElMessage.success('密码修改成功')
        formRef.value.resetFields()
      })
    }
  })
}
const saving = ref(true)
const privacy = reactive({
  phone:false,
  qq:false,
  wechat:false,
  email:false,
  gender:false
})


function savePrivacy(type,status){
  apiUserPrivacySave({
    type: type,
    status: status
  },saving)
}

onMounted(()=>{
  apiUserPrivacy(data => {
    Object.assign(privacy, data)//快速拷贝
    saving.value=false
  })
})

</script>

<template>

  <div style="margin: auto;max-width: 1000px;">
    <div style="margin-top: 20px">
      <card :icon="Setting" title="隐私设置" desc="在此设置展示哪些隐私信息" v-loading="saving">
        <div class="checkbox-list">
          <el-checkbox @change="savePrivacy('phone',privacy.phone)"
                       v-model="privacy.phone">公开展示手机号</el-checkbox>
          <el-checkbox @change="savePrivacy('email',privacy.email)"
                       v-model="privacy.email">公开展示电子邮件地址</el-checkbox>
          <el-checkbox @change="savePrivacy('qq',privacy.qq)"
                       v-model="privacy.qq">公开展示QQ账号</el-checkbox>
          <el-checkbox @change="savePrivacy('wechat',privacy.wechat)"
                       v-model="privacy.wechat">公开展示微信账号</el-checkbox>
          <el-checkbox @change="savePrivacy('gender',privacy.gender)"
                       v-model="privacy.gender">公开展示性别</el-checkbox>
        </div>
      </card>
      <card style="margin: 20px 0" :icon="Setting" title="修改密码" desc="在此设置修改账号密码，请牢记新密码">
        <el-form :rules="rules" ref="formRef" @validate="onValidate" :model="form" label-width="100px" style="margin:20px">
          <el-form-item label="当前密码" prop="password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.password" placeholder="当前密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password" placeholder="新密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input type="password" :prefix-icon="Lock" v-model="form.new_password_repeat" placeholder="再次输入新密码" maxlength="16"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button @click="resetPassword" :icon="Switch" type="success">重置密码</el-button>
          </div>
        </el-form>
      </card>
    </div>
  </div>

</template>

<style scoped>

.checkbox-list{
  margin: 10px 0 0 10px;
  display: flex;
  flex-direction: column;
}

</style>