export const orders = [
    {
        orderId: 12,
        email: 'customer@example.com',
        deliveryDate: '2026-11-27',
        status: 'ORDERED',
        totalPrice: 17000,
        items: [
            { productId: 1, name: 'Columbia Nariñó', price: 5000, quantity: 2 },
            { productId: 3, name: 'Ethiopia Yirgacheffe', price: 7000, quantity: 1 },
        ],
    },
    {
        orderId: 7,
        email: 'customer@example.com',
        deliveryDate: '2026-11-26',
        status: 'SHIPPED',
        totalPrice: 6000,
        items: [
            { productId: 2, name: 'Brazil Serra Do Caparaó', price: 6000, quantity: 1 },
        ],
    },
    {
        orderId: 5,
        email: 'customer@example.com',
        deliveryDate: '2026-11-25',
        status: 'CANCELED',
        totalPrice: 5000,
        items: [
            { productId: 1, name: 'Columbia Nariñó', price: 5000, quantity: 1 },
        ],
    },
]
