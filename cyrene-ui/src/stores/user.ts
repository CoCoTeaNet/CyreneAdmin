import { defineStore } from "pinia";

export const useUserStore = defineStore('user', {
    state: () => ({
        userinfo: {
            id: '',
            username: '',
            nickname: '',
            token: '',
            loginStatus: false,
            avatar: '',
            menuList: []
        }
    }),

    actions: {
        /**
         * 设置用户信息
         *
         * @param userModel 用户模型
         */
        setUserInfo(userModel: UserModel) {
            this.userinfo = userModel;
            localStorage.setItem("userInfo", JSON.stringify(userModel));
        },
        /**
         * 更新用户信息
         * 不包括菜单
         *
         * @param userModel 用户模型
         */
        updateUserInfo(userModel: UserModel) {
            userModel.menuList = this.userinfo.menuList;
            userModel.loginStatus = this.userinfo.loginStatus;
            userModel.token = this.userinfo.token;
            this.setUserInfo(userModel);
        }
    }
});
