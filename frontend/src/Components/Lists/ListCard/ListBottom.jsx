import { memo } from "react";
import { useSelector, useDispatch } from "react-redux";
import ListCard from "./ListCard";
import { useEffect } from "react";
import { getPrivateLists } from "../../../Store/List/Action";

const ListTop = memo(({ list }) => {
    const { theme } = useSelector((store) => store);
    const dispatch = useDispatch();
    // useEffect(() => {
    //     dispatch(getPrivateLists());
    // }, [])
    
    return (
    <div
        className="space-y-3"
        style={{ marginTop: 10 }}>
        Private Lists
        <hr
            className="overflow-y-scroll hideScrollbar border-gray-700 h-[20vh] w-full rounded-md"
            style={{
                marginTop: 10,
                background: 'grey',
                color: 'grey',
                borderColor: 'grey',
                height: '1px',
            }}
        />

        <section
            className={`${theme.currentTheme === "dark" ? "pt-14" : ""} space-y-5`}>
            {list?.lists?.map((item) => (
                item.privateMode ? (
                    <ListCard
                        style={{ marginTop: 10 }}
                        list={item} />
                ) : null
            ))}
        </section>
    </div>
    )
});

export default ListTop;