<template>
  <div class="markdown-renderer" v-html="renderedContent"></div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'

interface Props {
  content: string
}

const props = defineProps<Props>()

// 配置 marked
const renderer = new marked.Renderer()

// 自定义代码块渲染
renderer.code = ({ text, lang }: { text: string; lang?: string }) => {
  const validLanguage = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  const highlightedCode = hljs.highlight(text, { language: validLanguage }).value
  
  return `
    <div class="code-block-wrapper">
      <div class="code-block-header">
        <span class="code-language">${lang || 'text'}</span>
        <button class="copy-button" onclick="copyToClipboard(this)" data-code="${encodeURIComponent(text)}">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
            <path d="m5 15-4-4 4-4"></path>
          </svg>
          复制
        </button>
      </div>
      <pre><code class="hljs language-${validLanguage}">${highlightedCode}</code></pre>
    </div>
  `
}

// 自定义行内代码渲染
renderer.codespan = ({ text }: { text: string }) => {
  return `<code class="inline-code">${text}</code>`
}

// 自定义链接渲染（安全处理）
renderer.link = ({ href, title, tokens }: { href: string; title?: string | null; tokens: any[] }) => {
  const titleAttr = title ? ` title="${title}"` : ''
  const text = tokens.map(token => token.raw || token.text || '').join('')
  return `<a href="${href}" target="_blank" rel="noopener noreferrer"${titleAttr}>${text}</a>`
}

// 配置 marked 选项
marked.setOptions({
  renderer,
  breaks: true,
  gfm: true
})

// 渲染 Markdown 内容
const renderedContent = computed(() => {
  if (!props.content) return ''
  
  try {
    return marked.parse(props.content)
  } catch (error) {
    console.error('Markdown 解析错误:', error)
    return `<p>${props.content}</p>`
  }
})

// 添加全局复制函数
if (typeof window !== 'undefined') {
  (window as any).copyToClipboard = (button: HTMLElement) => {
    const code = decodeURIComponent(button.getAttribute('data-code') || '')
    navigator.clipboard.writeText(code).then(() => {
      const originalText = button.innerHTML
      button.innerHTML = `
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="20,6 9,17 4,12"></polyline>
        </svg>
        已复制
      `
      button.style.color = '#52c41a'
      
      setTimeout(() => {
        button.innerHTML = originalText
        button.style.color = ''
      }, 2000)
    }).catch(() => {
      console.error('复制失败')
    })
  }
}
</script>

<style scoped>
.markdown-renderer {
  line-height: 1.6;
  color: #333;
  font-size: 14px;
}

/* 标题样式 */
.markdown-renderer :deep(h1),
.markdown-renderer :deep(h2),
.markdown-renderer :deep(h3),
.markdown-renderer :deep(h4),
.markdown-renderer :deep(h5),
.markdown-renderer :deep(h6) {
  margin: 1.5em 0 0.5em 0;
  font-weight: 600;
  line-height: 1.4;
  color: #262626;
}

.markdown-renderer :deep(h1) {
  font-size: 1.5em;
  border-bottom: 2px solid #e8e8e8;
  padding-bottom: 0.3em;
}

.markdown-renderer :deep(h2) {
  font-size: 1.3em;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 0.2em;
}

.markdown-renderer :deep(h3) {
  font-size: 1.2em;
}

.markdown-renderer :deep(h4) {
  font-size: 1.1em;
}

.markdown-renderer :deep(h5),
.markdown-renderer :deep(h6) {
  font-size: 1em;
}

/* 段落样式 */
.markdown-renderer :deep(p) {
  margin: 0.8em 0;
  line-height: 1.7;
}

/* 列表样式 */
.markdown-renderer :deep(ul),
.markdown-renderer :deep(ol) {
  margin: 0.8em 0;
  padding-left: 2em;
}

.markdown-renderer :deep(li) {
  margin: 0.3em 0;
  line-height: 1.6;
}

.markdown-renderer :deep(ul li) {
  list-style-type: disc;
}

.markdown-renderer :deep(ol li) {
  list-style-type: decimal;
}

/* 引用样式 */
.markdown-renderer :deep(blockquote) {
  margin: 1em 0;
  padding: 0.8em 1em;
  border-left: 4px solid #1890ff;
  background: #f6f8ff;
  color: #595959;
  font-style: italic;
}

.markdown-renderer :deep(blockquote p) {
  margin: 0;
}

/* 行内代码样式 */
.markdown-renderer :deep(.inline-code) {
  background: #f5f5f5;
  color: #d73a49;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.9em;
  font-weight: 500;
}

/* 代码块容器样式 */
.markdown-renderer :deep(.code-block-wrapper) {
  margin: 1em 0;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  background: #fafafa;
}

.markdown-renderer :deep(.code-block-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #f0f0f0;
  border-bottom: 1px solid #e8e8e8;
  font-size: 12px;
}

.markdown-renderer :deep(.code-language) {
  color: #666;
  font-weight: 500;
  text-transform: uppercase;
}

.markdown-renderer :deep(.copy-button) {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  transition: all 0.2s ease;
}

.markdown-renderer :deep(.copy-button:hover) {
  background: #e6f7ff;
  color: #1890ff;
}

.markdown-renderer :deep(.code-block-wrapper pre) {
  margin: 0;
  padding: 16px;
  background: #ffffff;
  overflow-x: auto;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
  line-height: 1.5;
}

.markdown-renderer :deep(.code-block-wrapper code) {
  background: none;
  padding: 0;
  border-radius: 0;
  font-size: inherit;
}

/* 表格样式 */
.markdown-renderer :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
  font-size: 13px;
}

.markdown-renderer :deep(th),
.markdown-renderer :deep(td) {
  border: 1px solid #e8e8e8;
  padding: 8px 12px;
  text-align: left;
}

.markdown-renderer :deep(th) {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
}

.markdown-renderer :deep(tr:nth-child(even)) {
  background: #fafafa;
}

/* 链接样式 */
.markdown-renderer :deep(a) {
  color: #1890ff;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s ease;
}

.markdown-renderer :deep(a:hover) {
  border-bottom-color: #1890ff;
}

/* 分割线样式 */
.markdown-renderer :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8e8;
  margin: 2em 0;
}

/* 图片样式 */
.markdown-renderer :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 0.5em 0;
}

/* 强调样式 */
.markdown-renderer :deep(strong) {
  font-weight: 600;
  color: #262626;
}

.markdown-renderer :deep(em) {
  font-style: italic;
  color: #595959;
}

/* 删除线样式 */
.markdown-renderer :deep(del) {
  text-decoration: line-through;
  color: #8c8c8c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .markdown-renderer {
    font-size: 13px;
  }
  
  .markdown-renderer :deep(.code-block-wrapper pre) {
    padding: 12px;
    font-size: 12px;
  }
  
  .markdown-renderer :deep(table) {
    font-size: 12px;
  }
  
  .markdown-renderer :deep(th),
  .markdown-renderer :deep(td) {
    padding: 6px 8px;
  }
}
</style>