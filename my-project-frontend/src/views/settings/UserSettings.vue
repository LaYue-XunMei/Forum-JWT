<script setup>
import card from "@/components/Card.vue";
import {Message, Notebook, Refresh, Select, User} from "@element-plus/icons-vue"
import {userStore} from "@/store/index.js";
import {computed, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {post, get, accessHeader} from "@/net"
import axios from "axios";

const store = userStore()

const registerTime = computed(()=> new Date(store.user.registerTime).toLocaleString())

const desc = ref('')
const baseFormRef = ref()
const emailFormRef = ref()
const baseForm = reactive({
  username: '',
  gender: 0,
  phone: '',
  qq: '',
  wechat: '',
  desc: ''
})

const emailForm = reactive({
  email: '',
  code: ''
})
const validateUsername = (rule,value,callback) =>{
  if(value === ''){
    callback(new Error('请输入用户名'))
  }else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)){
    callback(new Error('用户名包含特殊字符，只能是中/英文'))
  }else{
    callback()
  }
}
const rules ={
  username:[
    {validator: validateUsername, trigger: ['blur','change']},
    {min:2,max:10, message: '长度在2到10个字符', trigger: ['blur','change']}
  ],
  email:[
    {required: true, message:'请输入邮箱', trigger: ['blur']},
    {type: 'email', message: '请输入合法的邮箱地址', trigger: ['blur','change']}
  ]
}

const loading = reactive({
  form: true,//用来表示页面的表单是否加载成功
  base: false
})

function saveDetails(){
  baseFormRef.value.validate(isValid=>{
    if(isValid){
      loading.base = true
      post("/api/user/save-details",baseForm,()=>{
        ElMessage.success("个人信息保存成功")
        store.user.username = baseForm.username
        desc.value = baseForm.desc
        loading.base = false
      },(message)=>{
        ElMessage.warning(message)
        loading.base = false
      });
    }
  })
}

get('/api/user/details',data =>{
  baseForm.username = store.user.username
  baseForm.gender = data.gender
  baseForm.phone = data.phone
  baseForm.qq = data.qq
  baseForm.wechat = data.wechat
  baseForm.desc = desc.value = data.desc

  emailForm.email = store.user.email
  //页面还没加载好，不能操作
  loading.form = false
})

const coldTime = ref(0)
const isEmailValid = ref(true)

const onValidate = (prop,isValid)=>{
  if(prop === 'email'){
    isEmailValid.value = isValid
  }
}

function sendEmailCode(){
  emailFormRef.value.validate(isValid=>{
    if(isValid){
      coldTime.value = 60
      get(`/api/auth/ask-code?email=${emailForm.email}&type=modify`,()=>{
        ElMessage.success("验证码已发送到您的邮箱")
        const handle = setInterval(()=>{
          coldTime.value--
          if(coldTime.value === 0){
            clearInterval(handle)
          }
        },1000)
      },(message)=>{
        ElMessage.warning(message)
        coldTime.value = 0
      })
    }
  })
}

function modifyEmail(){
  emailFormRef.value.validate(isValid=>{
    if(isValid){
      post("/api/user/modify-email",emailForm,()=>{
        ElMessage.success("邮箱修改成功")
        store.user.email = emailForm.email
        emailForm.code = ''
      })
    }
  })
}

function beforeAvatarUpload(rawFile){
  if(rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png'){
    ElMessage.error("只能上传jpg/png文件")
    return false
  }else if(rawFile.size > 5*1024*1024){
    ElMessage.error("图片大小不能超过5M")
    return false
  }
  return true
}

function upLoadSuccess(response){
  ElMessage.success("头像上传成功")
  store.user.avatar = response.data
}

</script>

<template>
  <div style="display: flex;max-width: 1000px;margin: auto">
    <div class="settings-left">
      <card :icon="User" title="账号信息设置" desc="在这里编辑个人信息，在隐私设置中选择是否展示这些信息" v-loading="loading.form">
        <el-form :model="baseForm" :rules="rules" ref="baseFormRef" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="baseForm.username" maxlength="10"/>
          </el-form-item>
          <el-form-item label="性别" >
            <el-radio-group v-model="baseForm.gender">
              <el-radio :label="0">男</el-radio>
              <el-radio :label="1">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="baseForm.phone" maxlength="11"/>
          </el-form-item>
          <el-form-item label="QQ" prop="qq">
            <el-input v-model="baseForm.qq" maxlength="13"/>
          </el-form-item>
          <el-form-item label="微信账号" prop="wechat">
            <el-input v-model="baseForm.wechat" maxlength="20"/>
          </el-form-item>
          <el-form-item label="个人简介" prop="desc">
            <el-input type="textarea" :rows="6" v-model="baseForm.desc" maxlength="200"/>
          </el-form-item>
          <div>
            <el-button :icon="Select" type="success" @click="saveDetails" :loading="loading.base">保存用户信息</el-button>
          </div>
        </el-form>
      </card>

      <card style="margin-top: 10px" :icon="Message" title="电子邮件设置" desc="在这里设置你的电子邮件">
        <el-form :model="emailForm" :rules="rules" @validate="onValidate" ref="emailFormRef" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="电子邮件" prop="email">
            <el-input v-model="emailForm.email"/>
          </el-form-item>
          <el-form-item prop="code">
            <el-row style="width: 100%" :gutter="10" >
              <el-col :span="18">
                <el-input placeholder="请获取验证码" v-model="emailForm.code"></el-input>
              </el-col>
              <el-col :span="6">
                <el-button @click="sendEmailCode" plain :disabled="!isEmailValid || coldTime >0" type="success" style="width: 100%">
                  {{ coldTime > 0 ? `请${coldTime}秒后再` : '获取验证码' }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button :icon="Refresh" type="success" @click="modifyEmail">更新电子邮件</el-button>
          </div>
        </el-form>
      </card>

    </div>

    <div class="settings-right">
      <div style="position: sticky;top: 20px">
        <card>
          <div style="text-align: center;padding:5px 15px 0 15px">
            <el-avatar :size="70"
                       :src="store.avatarUrl"/>
            <div style="margin: 5px 0">
              <el-upload :action="axios.defaults.baseURL+'/api/image/avatar'"
                         :show-file-list="false"
                         :before-upload="beforeAvatarUpload"
                         :on-success="upLoadSuccess"
                         :headers = "accessHeader()">
                <el-button  size="small" round>修改头像</el-button>
              </el-upload>
            </div>
            <div style="font-weight: bold">你好，{{store.user.username}}</div>
          </div>
          <el-divider style="margin: 10px 0"/>
          <div style="font-size: 14px ;color: grey;padding:10px">
            {{desc || '这个人很懒，什么都没有留下'}}
          </div>
        </card>
        <card style="margin-top: 10px;font-size: 14px" >
          <div>账号注册时间：{{registerTime}}</div>
          <div style="color: grey">欢迎加入我们的学习论坛！</div>
        </card>
      </div>

    </div>

  </div>
</template>

<style scoped>
.settings-left{
  flex: 1;
  margin: 20px;
}

.settings-right{
  width: 300px;
  margin: 20px 30px 20px 0;
}

</style>