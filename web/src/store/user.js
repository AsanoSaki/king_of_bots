import $ from "jquery";

export default {
  state: {  // 存储的信息
    id: "",
    username: "",
    photo: "",
    jwt_token: "",
    is_login: false,
  },
  getters: {},
  mutations: {  // 用来修改数据
    updateUser(state, user) {
      state.id = user.id;
      state.username = user.username;
      state.photo = user.photo;
      state.is_login = user.is_login;
    },
    updateJwtToken(state, jwt_token) {
      state.jwt_token = jwt_token;
    },
    clearState(state) {
      state.id = "";
      state.username = "";
      state.photo = "";
      state.jwt_token = "";
      state.is_login = false;
    },
  },
  actions: {
    login(context, data) {
      $.ajax({
        url: "http://localhost:3000/user/account/login/",
        type: "POST",
        data: {
          username: data.username,
          password: data.password,
        },
        success(resp) {
          if (resp.result === "success") {
            localStorage.setItem("jwt_token", resp.jwt_token);  // 将令牌存到LocalStorage中实现登录状态持久化
            context.commit("updateJwtToken", resp.jwt_token);
            data.success(resp);  // 成功后的回调函数
          }
        },
        error(resp) {
          data.error(resp);  // 失败后的回调函数
        },
      });
    },
    getInfo(context, data) {
      $.ajax({
        url: "http://localhost:3000/user/account/info/",
        type: "GET",
        async: false,
        headers: {
          // 不是固定的，是官方推荐的写法，Authorization是在我们的后端JwtAuthenticationTokenFilter类中设置的
          Authorization: "Bearer " + context.state.jwt_token,
        },
        success(resp) {
          if (resp.result === "success") {
            context.commit("updateUser", {
              ...resp,
              is_login: true,
            });
            data.success(resp);
          }
        },
        error(resp) {
          data.error(resp);
        },
      });
    },
    logout(context) {
      localStorage.removeItem("jwt_token");
      context.commit("clearState");
    },
  },
  modules: {},
};
