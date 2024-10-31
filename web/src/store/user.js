import $ from "jquery"

export default {
    state: {
        id: "",
        username: "",
        photo: "",
        is_login: false,
        token: "",
        // 目的是当用户登录成功后刷新不再显示瞬间的登录界面和登陆注册按钮
        // 只要规定一个参数，当用户登录成功后将该参数设置为false,其余情况为true即可
        // 并在按钮和界面中v-if判断是否显示
        pulling_info: true // 是否正在从云端拉取信息

    },
    getters: {
    },
    // 修改数据的构造函数
    mutations: {
        updateUser(state, user){
            state.id = user.id;
            state.username = user.username;
            state.photo = user.photo;
            state.is_login = user.is_login;
        },
        updateToken(state,token){
            state.token = token;

        },
        logout(state){
          state.id = "";
          state.username = "";
          state.photo = "";
          state.token = "";
          state.is_login = false;
        },
        updatePullingInfo(state,pulling_info){
          state.pulling_info = pulling_info;
        }
    },
    actions: {
        login(context, data){
            $.ajax({
                url: "http://localhost:3000/user/account/token/",
                type: "post",
                data: {
                  username: data.username,
                  password: data.password,
                },
                // resp联系后端，像token、error_message、success都是后端定义的
                success(resp){
                  // 调用函数时需要用commit调用
                  if (resp.error_message === "success") {
                    localStorage.setItem("jwt_token", resp.token);
                    context.commit("updateToken", resp.token); 
                    data.success(resp);
                  }else{
                    data.error(resp);
                  }
                },
                error(resp){
                    data.error(resp); 
                }
            });
        },
        logout(context){
          localStorage.removeItem("jwt_token");
          context.commit("logout");
        },
        getinfo(context, data){
            $.ajax({
                url: "http://localhost:3000/user/account/info/",
                type: "get",
                // 验证用
                headers: {
                  Authorization: "Bearer " + context.state.token,
                },
                success(resp){
                  if(resp.error_message === "success"){
                    context.commit("updateUser", {
                      // 将resp信息解构出来
                      ...resp,
                      is_login: true,
                    });
                    data.success(resp);
                  }
                  else{
                    data.error(resp);
                  }
                },
                error(resp){
                  data.error(resp);
                }
            })
        }
    },
    modules: {
    }
}