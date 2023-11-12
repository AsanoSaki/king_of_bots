import { createRouter, createWebHistory } from "vue-router";
import PKIndexView from "@/views/pk/PKIndexView";
import RecordIndexView from "@/views/record/RecordIndexView";
import RanklistIndexView from "@/views/ranklist/RanklistIndexView";
import MyBotsIndexView from "@/views/user/mybots/MyBotsIndexView";
import NotFoundView from "@/views/error/NotFoundView";

const routes = [
  {
    path: "/",
    name: "home",
    redirect: "/pk/",  // 如果是根路径则重定向到对战页面
  },
  {
    path: "/pk/",
    name: "pk_index",
    component: PKIndexView,
  },
  {
    path: "/record/",
    name: "record_index",
    component: RecordIndexView,
  },
  {
    path: "/ranklist/",
    name: "ranklist_index",
    component: RanklistIndexView,
  },
  {
    path: "/user/mybots/",
    name: "user_mybots_index",
    component: MyBotsIndexView,
  },
  {
    path: "/404/",
    name: "404",
    component: NotFoundView,
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

export default router;
