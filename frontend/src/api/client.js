import axios from 'axios'

const client = axios.create({
    baseURL: '/api',
    headers: { 'Content-Type': 'application/json' },
})

client.interceptors.response.use(
    (response) => response,
    (error) => {
        const message = error.response?.data?.message ?? '서버와 연결할 수 없습니다.'
        return Promise.reject(new Error(message))
    },
)

export default client