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
            permissions: [] as string[]
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
         *
         * @param userModel 用户模型
         */
        updateUserInfo(userModel: UserModel) {
            userModel.loginStatus = this.userinfo.loginStatus;
            userModel.token = this.userinfo.token;
            // 个人中心返回的用户信息不含权限集合，保留登录时下发的权限
            userModel.permissions = this.userinfo.permissions;
            this.setUserInfo(userModel);
        },
        /**
         * 判断是否拥有权限标识，超级管理员的权限集合为 ['*']
         *
         * @param permission 权限标识，如 system:user:add
         */
        hasPermission(permission: string): boolean {
            const permissions = this.userinfo.permissions ?? [];
            return permissions.includes('*') || permissions.includes(permission);
        }
    }
});
