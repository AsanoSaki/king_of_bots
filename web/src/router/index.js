import { createRouter, createWebHistory } from "vue-router";
import LocalPKIndexView from "@/views/pk/LocalPKIndexView";
import OnlinePKIndexView from "@/views/pk/OnlinePKIndexView";
import RecordIndexView from "@/views/record/RecordIndexView";
import RanklistIndexView from "@/views/ranklist/RanklistIndexView";
import MyBotsIndexView from "@/views/user/mybots/MyBotsIndexView";
import NotFoundView from "@/views/error/NotFoundView";
import UserAccountLoginView from "@/views/user/account/UserAccountLoginView";
import UserAccountRegisterView from "@/views/user/account/UserAccountRegisterView";
import store from "@/store/index";

const routes = [
  {
    path: "/",
    name: "home",
    redirect: "/pk/local/",  // 如果是根路径则重定向到对战页面
  },
  {
    path: "/pk/local/",
    name: "pk_local_index",
    component: LocalPKIndexView,
  },
  {
    path: "/pk/online/",
    name: "pk_online_index",
    component: OnlinePKIndexView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/record/",
    name: "record_index",
    component: RecordIndexView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/ranklist/",
    name: "ranklist_index",
    component: RanklistIndexView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/user/mybots/",
    name: "user_mybots_index",
    component: MyBotsIndexView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/user/account/login/",
    name: "user_account_login",
    component: UserAccountLoginView,
    meta: {
      requestAuth: false,
    },
  },
  {
    path: "/user/account/register/",
    name: "user_account_register",
    component: UserAccountRegisterView,
    meta: {
      requestAuth: false,
    },
  },
  {
    path: "/404/",
    name: "404",
    component: NotFoundView,
    meta: {
      requestAuth: false,
    },
  },
  {
    path: "/:catchAll(.*)",
    name: "others",
    redirect: "/404/",  // 如果不是以上路径之一说明不合法，重定向到404页面
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const jwt_token = localStorage.getItem("jwt_token");
  let jwt_token_valid = false;  // jwt_token是否存在且有效

  if (jwt_token) {  // jwt_token存在
    store.commit("updateJwtToken", jwt_token);
    store.dispatch("getInfo", {
      success() {  // jwt_token有效
        jwt_token_valid = true;
      },
      error() {
        alert("Invalid token! Please login!");
        store.dispatch("logout");  // 清除浏览器内存和LocalStorage中的jwt_token
        next({ name: "user_account_login" });
      },
    });
  }

  if (to.meta.requestAuth && !store.state.user.is_login && !jwt_token_valid) {
    alert("Please login!");
    next({ name: "user_account_login" });
  } else {
    next();  // 如果不需要授权就直接跳转即可
  }
});

export default router;
