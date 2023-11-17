<template>
  <div class="container">
    <div class="card" style="margin-top: 20px;">
      <div class="card-header">
        <h3 style="display: inline-block;">Login</h3>
        <div style="float: right; height: 2.5rem; line-height: 2.5rem;">
          <span>还没有账号？</span>
          <router-link :to="{ name: 'user_account_login' }" style="text-decoration: none;">
            去登录 >
          </router-link>
        </div>
        <div style="clear: both;"></div>
      </div>
      <div class="card-body">
        <div class="row justify-content-md-center">
          <div class="col-md-5">
            <div class="card" style="margin: 6rem auto; box-shadow: 5px 5px 20px #aaa;">
              <div class="card-header text-center">
                <h1>用户注册</h1>
              </div>
              <div class="row justify-content-md-center">
                <div class="col col-md-8">
                  <!-- @submit后的prevent是阻止掉submit的默认行为，防止组件间的向上或向下传递 -->
                  <form style="margin: 1rem;" @submit.prevent="register">
                    <div class="mb-3">
                      <label for="username" class="form-label">Username</label>
                      <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名" />
                    </div>
                    <div class="mb-3">
                      <label for="password" class="form-label">Password</label>
                      <input v-model="password" type="password" class="form-control" id="password" placeholder="请输入密码" />
                    </div>
                    <div class="mb-3">
                      <label for="confirmedPassword" class="form-label">Confirmed Password</label>
                      <input v-model="confirmedPassword" type="password" class="form-control" id="confirmedPassword" placeholder="请再次输入密码" />
                    </div>
                    <div style="font-size: 1rem; color: red;">
                      {{ error_message }}
                    </div>
                    <div class="success_message" style="font-size: 1rem; color: green;">
                      {{ success_message }}
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px;">
                      注册
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
import $ from "jquery";
import { ref } from "vue";
import router from "@/router/index";

export default {
  setup() {
    let username = ref("");
    let password = ref("");
    let confirmedPassword = ref("");
    let error_message = ref("");
    let success_message = ref("");

    const register = () => {
      error_message.value = "";
      $.ajax({
        url: "http://localhost:3000/user/account/register/",
        type: "POST",
        data: {
          username: username.value,
          password: password.value,
          confirmedPassword: confirmedPassword.value,
        },
        success(resp) {
          console.log(resp);
          if (resp.result === "success") {
            success_message.value = "Success! Go to home page after 3 seconds...";
            $(".success_message").fadeIn();
            setTimeout(() => {  // 设置3秒定时
              $(".success_message").fadeOut();
              router.push({ name: "home" });  // 跳转至home页面
            }, 3000);
          } else {
            error_message.value = resp.result;
          }
        },
        error(resp) {
          console.log(resp);
        },
      });
    };

    return {
      username,
      password,
      confirmedPassword,
      error_message,
      success_message,
      register,
    };
  },
};
</script>

<style scoped></style>
