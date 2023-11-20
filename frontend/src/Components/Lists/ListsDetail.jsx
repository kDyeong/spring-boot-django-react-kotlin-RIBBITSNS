import KeyboardBackspaceIcon from "@mui/icons-material/KeyboardBackspace";
import ProgressBar from "@ramonak/react-progress-bar";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router";
import { findListById } from "../../Store/List/Action";
import { findTwitsByListId } from "../../Store/Tweet/Action";
import TwitCard from "../Home/MiddlePart/TwitCard/TwitCard";

<<<<<<< HEAD
const ListsDetail = ({ changePage }) => {
  const param = useParams();
  const dispatch = useDispatch();
  const { list, twit, theme, auth } = useSelector((store) => store);
  // useSelector로 twit과 theme이라는 모듈의 상태값을 가져오도록 한 후, twit과 theme의 상태를 변경해서 궁극적으로 스토어의 상태를 변경
  // twit: twitReducer, theme: themeReducer
  // console.log("reply detail", twit.twit?.replyTwits.slice().reverse());
  const navigate = useNavigate();
  const handleBack = () => navigate(-1);
  // 뒤로가기, 앞으로가기는 navigate(1)
  useEffect(() => {
    dispatch(findListById(param.id));
    dispatch(findTwitsByListId(param.id));
  }, [param.id]);
=======
const ListsDetail = ({changePage, sendRefreshPage}) => {
    const param = useParams();
    const dispatch = useDispatch();
    const { list, twit, theme } = useSelector(store => store);
    // useSelector로 twit과 theme이라는 모듈의 상태값을 가져오도록 한 후, twit과 theme의 상태를 변경해서 궁극적으로 스토어의 상태를 변경
    // twit: twitReducer, theme: themeReducer
    // console.log("reply detail", twit.twit?.replyTwits.slice().reverse());
    const navigate = useNavigate();
    const handleBack = () => navigate(-1)
    // 뒤로가기, 앞으로가기는 navigate(1)
    useEffect(() => {
        dispatch(findListById(param.id))
        dispatch(findTwitsByListId(param.id))
    }, [param.id, sendRefreshPage])
>>>>>>> 1f9597c6ce5e969f08b0a77bdb7f1060ba520701

  console.log("listname check", list);

  const [totalEthicRateMAX, setTotalEthicRateMAX] = useState(0);
  const [averageEthicRateMAX, setAverageEthicRateMAX] = useState(0);

  useEffect(() => {
    // Calculate total ethicrateMAX
    const totalEthicRateMAXValue = twit.twits.reduce((sum, tweet) => {
      // ethiclabel이 4인 경우 0으로 포함하여 합산
      return sum + (tweet.ethiclabel === 4 ? 0 : tweet.ethicrateMAX || 0);
    }, 0);

    // Calculate average ethicrateMAX
    const averageEthicRateMAXValue =
      twit.twits.length > 0 ? totalEthicRateMAXValue / twit.twits.length : 0;

    // 정수로 변환
    const roundedAverageEthicRateMAX = Math.floor(averageEthicRateMAXValue);

    // 상태 업데이트
    setTotalEthicRateMAX(totalEthicRateMAXValue);
    setAverageEthicRateMAX(roundedAverageEthicRateMAX);

    // ... (다른 코드)
  }, [twit.twits, auth.user]);

  return (
    <div>
       <div className="flex">
        <section
          className={`z-50 flex items-center sticky top-0 ${
            theme.currentTheme === "light" ? "light" : "dark"
          } ${theme.currentTheme === "dark" ? " bg-[#0D0D0D]" : "bg-white"}`}
        >
          <KeyboardBackspaceIcon
            className="cursor-pointer"
            onClick={handleBack}
          />
          <div className="ml-5 flex" style={{ minWidth: "200px", flex: 1 }}>
            <h1 className="py-5 text-xl font-bold opacity-90 overflow-hidden">
              {list.list?.listName}
            </h1>
          </div>
        </section>

        <div className="flex mt-5 ml-auto" style={{ width: "200px" }}>
          {`${
            averageEthicRateMAX < 25
              ? "😄"
              : averageEthicRateMAX < 50
              ? "😅"
              : averageEthicRateMAX < 75
              ? "☹️"
              : "🤬"
          }`}
          <ProgressBar
            completed={averageEthicRateMAX}
            width="165px" // Set the fixed width for ProgressBar
            margin="2px 4px 4px 0" // Margin to right-align the ProgressBar
            bgColor={`${
              averageEthicRateMAX < 25
                ? "hsla(195, 100%, 35%, 0.8)"
                : averageEthicRateMAX < 50
                ? "hsla(120, 100%, 25%, 0.7)"
                : averageEthicRateMAX < 75
                ? "hsla(48, 100%, 40%, 0.8)"
                : "red"
            }`}
          />
        </div>
      </div>
      <section>
        <img
          className="w-[100%] h-[15rem] object-cover"
          src={
            list.findUser?.backgroundImage ||
            "https://png.pngtree.com/thumb_back/fw800/background/20230304/pngtree-green-base-vector-smooth-background-image_1770922.jpg"
          }
          alt=""
          loading="lazy"
        />
      </section>
      <div style={{ marginTop: 20 }}>
        {twit.twits && twit.twits.length > 0 ? (
          twit.twits.map((item) => (
            <TwitCard twit={item} key={item.id} changePage={changePage} />
          ))
        ) : (
          <div>게시된 리빗이 없습니다.</div>
        )}
      </div>
    </div>
  );
};

export default ListsDetail;
