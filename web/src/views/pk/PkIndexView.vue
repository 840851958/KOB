<template>
    <!-- 全局变量在tem中写要加$，script中不用 -->
    <PlayGround v-if="$store.state.pk.status === 'playing'"/>
    <MatchGround v-if="$store.state.pk.status === 'matching'"/>
    <ResultBoard v-if="$store.state.pk.loser != 'none'"/>
</template>

<script>
import PlayGround from "@/components/PlayGround.vue"
import MatchGround from "@/components/MatchGround.vue";
import ResultBoard from "@/components/ResultBoard.vue"
/* import ContentField from "@/components/ContentField.vue" */
// 组件被挂载和被卸载所执行的函数
import { onMounted,onUnmounted } from "vue";
import { useStore } from "vuex";
    export default{
        components: {
            /* ContentField */
            PlayGround,
            MatchGround,
            ResultBoard,
        },
        setup() {
        const store = useStore();
        const socketUrl = `ws://localhost:3000/websocket/${store.state.user.token}/`;
        
        store.commit("updateLoser", "none");

        let socket = null;
        onMounted(() => {
            store.commit("updateopponent", {
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
            })
            socket = new WebSocket(socketUrl); //页面被挂载后，成功创建了一个websocket，因此需要更新全局变量，所以还得写一个辅助函数在pk中

            socket.onopen = () =>{
                console.log("connected!");
                store.commit("updateSocket",socket);  
            }
            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                //这里event和st~都是后端定义的名字
                if(data.event === "start_matching"){
                    store.commit("updateopponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    // setTimeout设置延迟2s后执行
                    setTimeout(() => {
                        store.commit("updateStatus","playing");
                    },200);
                    store.commit("updateGame",data.game);
                }else if(data.event === "move"){ // 首先得找到两条蛇，蛇定义在了gamemap中的snakes
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes; // 解构
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                }else if(data.event === "result"){
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes; // 解构
                    if (data.loser === "all" || data.loser === "A") {
                        snake0.status = "die";
                    }
                    if (data.loser === "all" || data.loser === "B") {
                        snake1.status = "die";
                    }
                    store.commit("updateLoser", data.loser);
                }
            }
            socket.onclose = () => {
                console.log("disconnected!");
                
            }
        });

        onUnmounted(() => {
            socket.close(); 
            store.commit("updateStatus","matching");
        });
        }
    }
    

</script>

<style scoped>
</style>