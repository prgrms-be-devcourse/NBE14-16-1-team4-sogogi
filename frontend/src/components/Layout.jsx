import { Link, Outlet } from 'react-router-dom'

export default function Layout(){
    return(
        <div>
            <header>
                <Link to="/">Grids & Circles</Link>
                <nav>
                    <Link to="/">주문하기</Link>
                    <Link to="/orders">내 주문</Link>
                    <Link to="/admin/products">상품 관리</Link>
                    <Link to="/admin/orders">주문 관리</Link>
                </nav>
            </header>
            <main>
                <Outlet />
            </main>
        </div>
    )
}