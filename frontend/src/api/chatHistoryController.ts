// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /chatHistory/app/${param0} */
export async function listAppChatHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listAppChatHistoryParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponsePageChatHistory>(`/chatHistory/app/${param0}`, {
    method: 'GET',
    params: {
      // pageSize has a default value: 10
      pageSize: '10',
      ...queryParams,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /chatHistory/app/${param0} */
export async function deleteChatHistoryByAppId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteChatHistoryByAppIdParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/chatHistory/app/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/delete */
export async function deleteChatHistory(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/chatHistory/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /chatHistory/get */
export async function getChatHistoryById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getChatHistoryByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseChatHistory>('/chatHistory/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /chatHistory/get/vo */
export async function getChatHistoryVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getChatHistoryVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseChatHistoryVO>('/chatHistory/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /chatHistory/latest/${param0} */
export async function getLatestChatHistoryByAppId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLatestChatHistoryByAppIdParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseListChatHistoryVO>(`/chatHistory/latest/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/list/page/vo */
export async function listChatHistoryVoByPage(
  body: API.ChatHistoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatHistoryVO>('/chatHistory/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/my/list/page/vo */
export async function listMyChatHistoryVoByPage(
  body: API.ChatHistoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatHistoryVO>('/chatHistory/my/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
