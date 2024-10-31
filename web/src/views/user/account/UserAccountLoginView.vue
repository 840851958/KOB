<template>
    <ContentField> 
        <div class="row justify-content-md-center" v-if="!$store.state.user.pulling_info">
            <div class="col-3">
                <form @submit.prevent="login">
                    <div class="mb-3">
                        <label for="username" class="form-label">用户名</label>
                        <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">密码</label>
                        <input v-model="password" type="password" class="form-control" id="password" placeholder="请输入密码">
                    </div>
                    <div class="error-message">{{ error_message }}</div>
                    <button type="submit" class="btn btn-primary">提交 </button>
                </form>
            </div>
        </div>
    </ContentField>
</template>

<script>
import ContentField from "@/components/ContentField.vue"
import { useStore } from "vuex"
import { ref } from "vue"
import router from "@/router"

export default{
    components: {
        ContentField 
    },
    setup(){
        const store = useStore();
        // ref 引用元素
        let username = ref('');
        let password = ref('');
        let error_message = ref('');

        const jwt_token = localStorage.getItem("jwt_token");
        if (jwt_token) {
            // 调用mutation中用commit，actions中用dispatch 
            store.commit("updatePullingInfo", true);
            store.commit("updateToken", jwt_token);
            store.dispatch("getinfo", {
                success(){
                    router.push({name: "home"});
                    // 有什么写的必要性？
                    // 目前写不写都不会影响UI闪烁，但为了保证变量的含义-正在拉取用户信息。所以当成功登录后拉取结束，还是变成false好点，保证了一致性，并且由于成功登录会将is_login修改为true不影响UI
                    store.commit("updatePullingInfo",false);
                },
                error(){
                    store.commit("updatePullingInfo",false);
                }
            })
        }else{
            // 第一次登录没有token
            store.commit("updatePullingInfo",false);
        }

        // 触发函数
        const login = () => {
            error_message.value = "";
            // 调用user.js的login函数需要用dispatch
            store.dispatch("login", {
                username: username.value,
                password: password.value,
                success(){
                    store.dispatch("getinfo", {
                        success(){
                            router.push({name: "home"});
                            console.log(store.state.user);
                        }
                    })
                },
                error(){
                    error_message.value = "用户名或密码错误";
                }
            })
        }

        return {
            username,
            password,
            error_message,
            login,
        }
    }
}
</script>

<style scoped>
    button {
        width: 100%;
    }
    div.error-message{
        color: red;
    }
</style>