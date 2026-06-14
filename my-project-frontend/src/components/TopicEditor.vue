<script setup>
import {computed, reactive, ref} from "vue";
import {Quill, QuillEditor} from "@vueup/vue-quill";
import ImageResize from 'quill-image-resize-vue';
import {ImageExtend, QuillWatch} from  "quill-image-super-solution-module"
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {Check} from "@element-plus/icons-vue";
import axios from "axios";
import {get, post, accessHeader} from "@/net/index.js";
import {ElMessage} from "element-plus";
import ColorDot from "@/components/ColorDot.vue";

defineProps({
  show: Boolean
})

const emits = defineEmits(["close","success"])

const editor = reactive({
  type: null,
  title: "",
  text: "",
  loading:false,
  types : []
})

const refEditor =ref()

function initEditor(){
  refEditor.value.setContents('','user')
  editor.title = ""
  editor.type = null
}

function deltaToText(delta){
  if(!delta.ops) return ""
  let str = ""
  for(let op of delta.ops)
    str += op.insert
  return str.replace(/\s/g,"")
}

const contentLength = computed(()=>deltaToText(editor.text).length)

get('/api/forum/types',data =>editor.types = data)

function submitTopic(){
  const text = deltaToText(editor.text)
  if(text.length>20000){
    ElMessage.error("帖子内容过长，请重新编辑")
    return
  }
  if(!editor.title){
    ElMessage.error("帖子标题不能为空")
    return
  }
  if(!editor.type){
    ElMessage.error("帖子类型不能为空，请选择一个类型分区发布")
    return
  }
  post('/api/forum/create-topic', {
    type: editor.type.id, //发帖请求体的 type绑定的是整个item对象，所以用id拿出来
    title: editor.title,
    content: editor.text
  },()=>{
    ElMessage.success("帖子发表成功")
    emits('success')
  })

}

Quill.register('modules/imageResize', ImageResize);
Quill.register('modules/imageExtend', ImageExtend);

const editorOption = {
  modules:{
    toolbar:{
      container:[
        "bold", "italic", "underline", "strike", "clean",
        {color:[]},{'background':[]},
        {size:["small",false,"large","huge"]},
        {header:[1,2,3,4,5,6,false]},
        {list:"ordered"},{list:"bullet"},{align:[]},
        "blockquote","code-block","link","image",
        {indent:'-1'},{indent:'+1'}
      ],
      handlers:{
        'image' : function (){
          QuillWatch.emit(this.quill.id)
        }
      }
    },
    imageResize:{
      modules:['Resize', 'DisplaySize']
    },
    imageExtend: {
      action: axios.defaults.baseURL+'/api/image/cache',
      name: 'file',
      size:5,
      loading:true,
      accept:'image/jpeg, image/png',
      response:(resp)=>{
        if(resp.data){
          return axios.defaults.baseURL +'/images'+resp.data
        }else{
          return null
        }
      },
      methods : 'POST',
      headers: xhr =>{
        xhr.setRequestHeader("Authorization",accessHeader().Authorization);
      },
      start:()=>editor.loading= true,
      success:()=>{
        ElMessage.success("图片上传成功")
        editor.loading = false
      },
      error:()=>{
        ElMessage.warning("图片上传失败")
        editor.loading = false
      }
    }
  }
}

</script>

<template>
  <div>
    <el-drawer :model-value="show"
               direction="btt"
               @open="initEditor"
               :close-on-click-modal="false"
               :size="700"
               @close="emits('close')">
      <template #header>
        <div>
          <div style="font-weight: bold">发表新的帖子</div>
          <div style="font-size: 13px">发表帖子请遵守相关规定</div>
        </div>
      </template>
      <div style="display: flex;gap: 10px">
        <div style="width: 150px">
          <el-select placeholder="请选择主题类型..." value-key="id" v-model="editor.type"  :disabled="!editor.types.length">
            <el-option v-for ="item in editor.types" :value="item" :label="item.name">
              <div>
                <color-dot :color="item.color"/>
                <span style="margin-left: 5px">{{item.name}}</span>
              </div>
            </el-option>
          </el-select>

        </div>
        <div style="flex:1">
          <el-input v-model="editor.title"
                    placeholder="请输入帖子标题..."
                    :prefix-icon="Document"
                    maxlength="30"/>
        </div>
      </div>
      <div style="margin-top: 5px;font-size: 13px;color: grey">
        <color-dot :color="editor.type ? editor.type.color : '#dedede'"/>
        <span style="margin-left: 5px">{{editor.type ? editor.type.desc : '请在上方选择一个帖子类型'}}</span>
      </div>

      <div style="margin-top: 10px;height: 450px;overflow: hidden;border-radius: 5px"
           v-loading="editor.loading"
           element-loading-text="正在上传图片，请稍后...">
        <quill-editor v-model:content="editor.text" style="height: calc(100% - 45px)"
                      placeholder="请输入帖子内容..."
                      content-type="delta" ref="refEditor"
                      :options="editorOption">
        </quill-editor>
      </div>
      <div style="display: flex;justify-content: space-between;margin-top: 10px">
        <div style="font-size: 13px;color: grey">当前字数 {{contentLength}} || 最大支持 10000</div>
        <div>
          <el-button @click="submitTopic" type="success" :icon="Check" plain>立即发表帖子</el-button>
        </div>
      </div>

    </el-drawer>
  </div>

</template>

<style scoped>
:deep(.el-drawer){
  width: 900px;
  margin: auto;
  border-radius: 10px 10px 0 0;
}
:deep(.el-drawer__header){
  margin: 0;
}

:deep(.ql-toolbar){
  border-radius: 5px 5px 0 0;
  border-color: var(--el-border-color);
}

:deep(.ql-container){
  border-radius: 0 0 5px 5px;
  border-color: var(--el-border-color);
}

:deep(.ql-editor.ql-blank::before){
  color: var(--el-text-color-placeholder);
  font-style: normal;
}

:deep(.ql-editor){
  font-size: 16px;
}

</style>