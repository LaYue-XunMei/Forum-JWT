<script setup>
import {Delta, QuillEditor} from "@vueup/vue-quill";
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {computed, ref} from "vue";
import {post} from "@/net";
import {ElMessage} from "element-plus";

const props = defineProps({
  show:Boolean,
  tid: String,
  quote:Object
})
const content = ref()

const emits = defineEmits(["close","comment"])

const init = () => content.value = new Delta()

function submitComment(){
  if(deltaToText(content.value).length > 2000){
    ElMessage.warning("评论字数超出最大字数限制")
    return
  }
  post('/api/forum/add-comment',{
    tid:props.tid,
    quote:props.quote ? props.quote.id : -1,//这里的quote就是一条评论，返回id保存即可
    content: JSON.stringify(content.value)
  },()=>{
    ElMessage.success("发表评论成功")
    emits('comment')
  })
}

function deltaToSimpleText(delta){
  let str = deltaToText(JSON.parse(delta))
  if(str.length > 35)
    str = str.substring(0,35)+"...."
  return str
}

function deltaToText(delta){
  if(!delta?.ops) return ""
  let str = ""
  for(let op of delta.ops)
    str += op.insert
  return str.replace(/\s/g,"")
}

const contentLength = computed(()=>deltaToText(editor.text).length)
</script>

<template>
  <div>
    <el-drawer :model-value="show"
               :title="quote ? `发表对评论：${deltaToSimpleText(quote.content)} 的回复` : '发表帖子的评论' "
               @open="init"
               @close="emits('close')"
               direction="btt"
               :size="270"
               :close-on-click-modal="false">
      <div>
        <div>
          <quill-editor style="height: 120px" v-model:content="content"
                        placeholder="发表一条评论"/>
        </div>
        <div style="margin-top: 10px;text-align:right;display:flex ">
          <div style=" flex:1;font-size: 13px;color: grey;text-align: left">当前字数 {{deltaToText(content).length}} || 最大支持 10000</div>
          <el-button @click="submitComment" type="success" plain>发表评论</el-button>
        </div>
      </div>

    </el-drawer>
  </div>
</template>

<style lang="less" scoped>
:deep(.el-drawer){
  width: 800px;
  margin: 20px auto;
  border-radius: 10px;
}
:deep(.el-drawer__header){
  margin: 0;
}
:deep(.el-drawer__body){
  padding: 10px;
}

</style>