import KeyboardBackspaceIcon from "@mui/icons-material/KeyboardBackspace";
import { Divider } from "@mui/material";
import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import { findTwitsById } from "../../../Store/Tweet/Action";
import TwitCard from "./TwitCard/TwitCard";

const TwitDetail = ({changePage, sendRefreshPage}) => {
    const param=useParams();
    // twit/83
    const dispatch=useDispatch();
    const {twit}=useSelector(store=>store);
    // useSelector로 twit과 theme이라는 모듈의 상태값을 가져오도록 한 후, twit과 theme의 상태를 변경해서 궁극적으로 스토어의 상태를 변경
    // twit: twitReducer, theme: themeReducer
    const navigate=useNavigate();

    const handleBack = () => navigate(-1)
    // 뒤로가기, 앞으로가기는 navigate(1)
    useEffect(()=>{
        dispatch(findTwitsById(param.id))
    },[param.id, sendRefreshPage])

  console.log("twitdetail twit check", twit);

  useEffect(() => {
    const messageEventListener = (event) => {
      const message = event.data;

      if (message.type === "navigate") {
        // 메시지가 navigate 타입일 때만 경로 변경
        navigate(message.path);
      }
    };

    window.addEventListener("message", messageEventListener);

    return () => {
      window.removeEventListener("message", messageEventListener);
    };
  }, [navigate]);

  return (
    <div>
        <section
        className={`z-50 flex items-center sticky top-0 bg-opacity-95`}
      >
        <KeyboardBackspaceIcon
          className="cursor-pointer"
          onClick={handleBack}
        />
        <h1 className="py-5 text-xl font-bold opacity-90 ml-5 ${}">{"리빗"}</h1>
      </section>
      {twit.twit && <TwitCard twit={twit.twit} changePage={changePage} />}
      {/* twit.twit가 참이라면 TwitCard 렌더링 됨 */}
      <Divider sx={{ margin: "2rem 0rem" }} />

      <div>
        {twit.twit?.replyTwits
          .slice()
          .reverse()
          .map((item) => (
            <TwitCard twit={item} key={item.id} changePage={changePage} />
          ))}
        {/* twit.twit notnull 일때 replyTwits 역순 배열 */}
      </div>

      <ToastContainer
        position="bottom-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
      />
      
    </div>
  );
};

export default TwitDetail;
