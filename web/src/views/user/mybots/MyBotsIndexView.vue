<template>
  <div class="container">
    <div class="card text-bg-secondary" style="margin-top: 20px;">
      <div class="card-header">
        <h3>My Bots</h3>
      </div>
      <div class="card-body" style="background-color: rgba(255, 255, 255, 0.5);">
        <div class="row">
          <div class="col-md-3">
            <div class="card">
              <!-- 用户头像与用户名 -->
              <div class="card-body text-center">
                <img class="img-fluid" :src="$store.state.user.photo" style="width: 50%; border-radius: 50%;">
                <div style="font-size: 20px; margin-top: 10px; font-weight: bold;">
                  {{ $store.state.user.username }}
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-9">
            <div class="card">
              <div class="card-header">
                <span style="font-size: 25px;">Bot 管理</span>
                <!-- 点击创建按钮能够打开模态框 -->
                <button class="btn btn-outline-success float-end" type="button" data-bs-toggle="modal" data-bs-target="#add_bot_modal">
                  创建 Bot
                </button>
                <!-- 创建 Bot 的模态框 -->
                <div class="modal fade" id="add_bot_modal" tabindex="-1">
                  <div class="modal-dialog modal-xl">
                    <div class="modal-content">
                      <div class="modal-header" style="background-color: #198754;">
                        <h1 class="modal-title fs-5" style="color: white;">创建 Bot</h1>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                      </div>
                      <div class="modal-body">
                        <div class="mb-3">
                          <label for="title" class="form-label">名称</label>
                          <input v-model="new_bot.title" type="email" class="form-control" id="title" placeholder="请输入 Bot 名称">
                        </div>
                        <div class="mb-3">
                          <label for="description" class="form-label">简介</label>
                          <textarea v-model="new_bot.description" class="form-control" id="description" rows="3" placeholder="介绍一下你的 Bot 吧~（可以暂时不填）"></textarea>
                        </div>
                        <div class="mb-3">
                          <label for="content" class="form-label">代码</label>
                          <VAceEditor
                            v-model:value="new_bot.content"
                            @init="editorInit"
                            lang="c_cpp"
                            theme="textmate"
                            style="height: 400px"
                            :options="{
                              enableBasicAutocompletion: true,  //启用基本自动完成
                              enableSnippets: true,  // 启用代码段
                              enableLiveAutocompletion: true,  // 启用实时自动完成
                              fontSize: 16,  //设置字号
                              tabSize: 4,  // 标签大小
                              showPrintMargin: false,  //去除编辑器里的竖线
                              highlightActiveLine: true,  // 选中行高亮显示
                            }"
                          />
                        </div>
                      </div>
                      <div class="modal-footer">
                        <div class="error_message" style="color: red;">{{ new_bot.error_message }}</div>
                        <button type="button" class="btn btn-success" @click="add_bot">创建</button>
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <!-- 用户的 Bot 列表 -->
              <div class="card-body">
                <table class="table table-striped table-hover">
                  <thead>
                    <tr>
                      <th>名称</th>
                      <th>创建时间</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="bot in bots" :key="bot.id">
                      <td style="line-height: 32px;">{{ bot.title }}</td>
                      <td style="line-height: 32px;">{{ bot.createtime }}</td>
                      <td>
                        <button class="btn btn-outline-primary" type="button" data-bs-toggle="modal" :data-bs-target="'#update_bot_modal_' + bot.id" style="margin-right: 10px; padding: 3px 10px;">
                          修改
                        </button>
                        <button class="btn btn-outline-danger" type="button" data-bs-toggle="modal" :data-bs-target="'#remove_bot_modal_' + bot.id" style="padding: 3px 10px;">
                          删除
                        </button>
                        <!-- 修改 Bot 的模态框 -->
                        <div class="modal fade" :id="'update_bot_modal_' + bot.id" tabindex="-1">
                          <div class="modal-dialog modal-xl">
                            <div class="modal-content">
                              <div class="modal-header" style="background-color: #0D6EFD;">
                                <h1 class="modal-title fs-5" style="color: white;">修改 Bot</h1>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                              </div>
                              <div class="modal-body">
                                <div class="mb-3">
                                  <label for="title" class="form-label">名称</label>
                                  <input v-model="bot.title" type="email" class="form-control" id="title" placeholder="请输入 Bot 名称">
                                </div>
                                <div class="mb-3">
                                  <label for="description" class="form-label">简介</label>
                                  <textarea v-model="bot.description" class="form-control" id="description" rows="3" placeholder="介绍一下你的 Bot 吧~（可以暂时不填）"></textarea>
                                </div>
                                <div class="mb-3">
                                  <label for="content" class="form-label">代码</label>
                                  <VAceEditor
                                    v-model:value="bot.content"
                                    @init="editorInit"
                                    lang="c_cpp"
                                    theme="textmate"
                                    style="height: 400px"
                                    :options="{
                                      enableBasicAutocompletion: true,
                                      enableSnippets: true,
                                      enableLiveAutocompletion: true,
                                      fontSize: 16,
                                      tabSize: 4,
                                      showPrintMargin: false,
                                      highlightActiveLine: true,
                                    }"
                                  />
                                </div>
                              </div>
                              <div class="modal-footer">
                                <div class="error_message" style="color: red;">{{ new_bot.error_message }}</div>
                                <button type="button" class="btn btn-primary" @click="update_bot(bot)">保存修改</button>
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                              </div>
                            </div>
                          </div>
                        </div>
                        <!-- 删除 Bot 的确认模态框 -->
                        <div class="modal fade" :id="'remove_bot_modal_' + bot.id" tabindex="-1">
                          <div class="modal-dialog modal-dialog-centered">
                            <div class="modal-content">
                              <div class="modal-header" style="background-color: #DC3545;">
                                <h1 class="modal-title fs-5" style="color: white;">警告</h1>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                              </div>
                              <div class="modal-body" style="font-size: 20px;">
                                确认删除？该操作无法撤销哦~
                              </div>
                              <div class="modal-footer">
                                <button type="button" class="btn btn-danger" @click="remove_bot(bot)">删除</button>
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from "vue";  // reactive用来让组件绑定对象
import { useStore } from "vuex";
import { Modal } from "bootstrap/dist/js/bootstrap";
import $ from "jquery";
import { VAceEditor } from "vue3-ace-editor";
import ace from "ace-builds";
import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/theme-chrome";
import "ace-builds/src-noconflict/ext-language_tools";
import "ace-builds/src-noconflict/mode-c_cpp";

export default {
  components: {
    VAceEditor,
  },

  setup() {
    ace.config.set(
      "basePath",
      "https://cdn.jsdelivr.net/npm/ace-builds@" +
      require("ace-builds").version +
      "/src-noconflict/");

    const store = useStore();
    let bots = ref([]);  // bot列表

    const new_bot = reactive({  // 要新创建的bot对象
      title: "",
      description: "",
      content: "",
      error_message: "",
    });

    const get_bots = () => {
      $.ajax({
        url: "http://localhost:3000/user/bot/getlist/",
        type: "GET",
        headers: {
          Authorization: "Bearer " + store.state.user.jwt_token,
        },
        success(resp) {
          bots.value = resp;  // 后端返回的就是一个列表
        },
      });
    };

    get_bots();  // 定义完后直接执行一遍

    const add_bot = () => {
      new_bot.error_message = "";
      $.ajax({
        url: "http://localhost:3000/user/bot/add/",
        type: "POST",
        data: {
          title: new_bot.title,
          description: new_bot.description,
          content: new_bot.content,
        },
        headers: {
          Authorization: "Bearer " + store.state.user.jwt_token,
        },
        success(resp) {
          if (resp.result === "success") {
            // 需要清空信息防止下次打开创建模态框时还留有上次的信息
            new_bot.title = "";
            new_bot.description = "";
            new_bot.content = "";
            Modal.getInstance("#add_bot_modal").hide();  // 关闭模态框
            get_bots();  // 刷新一下Bot列表
          } else {
            new_bot.error_message = resp.result;
          }
        },
      });
    };

    const remove_bot = (bot) => {
      $.ajax({
        url: "http://localhost:3000/user/bot/remove/",
        type: "POST",
        data: {
          bot_id: bot.id,
        },
        headers: {
          Authorization: "Bearer " + store.state.user.jwt_token,
        },
        success(resp) {
          if (resp.result === "success") {
            Modal.getInstance("#remove_bot_modal_" + bot.id).hide();
            get_bots();
          }
        },
      });
    };

    const update_bot = (bot) => {
      new_bot.error_message = "";
      $.ajax({
        url: "http://localhost:3000/user/bot/update/",
        type: "POST",
        data: {
          bot_id: bot.id,
          title: bot.title,
          description: bot.description,
          content: bot.content,
        },
        headers: {
          Authorization: "Bearer " + store.state.user.jwt_token,
        },
        success(resp) {
          if (resp.result === "success") {
            Modal.getInstance("#update_bot_modal_" + bot.id).hide();
            get_bots();
          } else {
            new_bot.error_message = resp.result;
          }
        },
      });
    };

    return {
      bots,
      new_bot,
      add_bot,
      remove_bot,
      update_bot,
    };
  },
};
</script>

<style scoped></style>
