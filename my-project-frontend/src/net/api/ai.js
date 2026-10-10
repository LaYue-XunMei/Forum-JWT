import {fetchPost, post} from "@/net";

export const apiChatWithAI = async (context, onMessage, onError, onComplete) => {
    //变成异步
    try{
        const response = await fetchPost('/api/ai/chat',context)
        const reader = response.body.getReader();//得到的是类似byte数组
        const decoder = new TextDecoder();

        while(true){
            const { done,value } = await reader.read();//reader.read()返回对象表示是否读完已经内容
            if(done) break;
            onMessage(decoder.decode(value)
                .replaceAll("data:","")
                .replaceAll("\n",""));//直接拿最终的文本
        }
        onComplete()
    }catch(e){
        onError(e)
    }
}
