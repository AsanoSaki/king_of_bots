<template>
  <div class="container">
    <div class="card" style="margin-top: 20px;">
      <div class="card-header">
        <h3 style="display: inline-block;">Login</h3>
        <div style="float: right; height: 2.5rem; line-height: 2.5rem;">
          <span>还没有账号？</span>
          <router-link :to="{ name: 'user_account_register' }" style="text-decoration: none;">
            去注册 >
          </router-link>
        </div>
        <div style="clear: both;"></div>
      </div>
      <div class="card-body">
        <div class="row justify-content-md-center">
          <div class="col-md-5">
            <div class="card" style="margin: 6rem auto; box-shadow: 5px 5px 20px #aaa;">
              <div class="card-header text-center">
                <h1>用户登录</h1>
              </div>
              <div class="row justify-content-md-center">
                <div class="col col-md-8">
                  <!-- @submit后的prevent是阻止掉submit的默认行为，防止组件间的向上或向下传递 -->
                  <form style="margin: 1rem;" @submit.prevent="login">
                    <div class="mb-3">
                      <label for="username" class="form-label">Username</label>
                      <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名" />
                    </div>
                    <div class="mb-3">
                      <label for="password" class="form-label">Password</label>
                      <input v-model="password" type="password" class="form-control" id="password" placeholder="请输入密码" />
                    </div>
                    <div style="font-size: 1rem; color: red;">
                      {{ error_message }}
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px;">
                      登录
                    </button>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useStore } from "vuex";
import { ref } from "vue";
import router from "@/router/index";

export default {
  setup() {
    const store = useStore();
    let username = ref("");
    let password = ref("");
    let error_message = ref("");

    const login = () => {
      error_message.value = "";
      store.dispatch("login", {  // 使用dispatch调用store的actions中的函数
        username: username.value,  // ref变量取值用.value
        password: password.value,
        success(resp) {  // actions中的回调函数会返回resp
          console.log(resp);
          store.dispatch("getInfo", {
            success(resp) {
              console.log(resp);
              router.push({ name: "home" });  // 跳转至home页面
            },
          });
        },
        error(resp) {
          console.log(resp);
          error_message.value = "The username or password is wrong!";
        },
      });
    };

    return {
      username,
      password,
      error_message,
      login,
    };
  },
};
</script>

<style scoped></style>
