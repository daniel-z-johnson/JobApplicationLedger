import { NavLink } from 'react-router'

export default function Sidebar() {
  return <aside className="w-32 shrink-0 border-r border-gray-700 bg-gray-800 p-2 sm:w-52 sm:p-4">
    <nav aria-label="Main navigation">
      <NavLink
        to="/companies"
        className={({ isActive }) => `block rounded-md px-3 py-3 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-300 ${isActive ? 'bg-gray-700 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'}`}
      >
        Companies
      </NavLink>
    </nav>
  </aside>
}
