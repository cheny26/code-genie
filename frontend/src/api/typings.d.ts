declare namespace API {
  type App = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
    userName?: string
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    user?: UserVO
    createTime?: string
    updateTime?: string
  }

  type BaseResponseApp = {
    code?: number
    data?: App
    message?: string
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseChatHistory = {
    code?: number
    data?: ChatHistory
    message?: string
  }

  type BaseResponseChatHistoryVO = {
    code?: number
    data?: ChatHistoryVO
    message?: string
  }

  type BaseResponseListChatHistoryVO = {
    code?: number
    data?: ChatHistoryVO[]
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePageChatHistoryVO = {
    code?: number
    data?: PageChatHistoryVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ChatHistory = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appId?: number
    userId?: number
    messageType?: string
    message?: string
    lastCreateTime?: string
  }

  type ChatHistoryVO = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    appName?: string
    userId?: number
    userName?: string
    createTime?: string
  }

  type chatToGenCodeParams = {
    appId: number
    message: string
  }

  type deleteChatHistoryByAppIdParams = {
    appId: number
  }

  type DeleteRequest = {
    id?: number
  }

  type getAppByIdParams = {
    id: number
  }

  type getAppVOByIdParams = {
    id: number
  }

  type getChatHistoryByIdParams = {
    id: number
  }

  type getChatHistoryVOByIdParams = {
    id: number
  }

  type getLatestChatHistoryByAppIdParams = {
    appId: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type listAppChatHistoryParams = {
    appId: number
    pageSize?: number
    lastCreateTime?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistoryVO = {
    records?: ChatHistoryVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type User = {
    id?: number
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    email?: string
    userRole?: string
    points?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userName?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    email?: string
    userName?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    points?: number
    email?: string
    userRole?: string
    createTime?: string
  }
}
