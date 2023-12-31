import AddIcon from "@mui/icons-material/Add";
import ChatIcon from "@mui/icons-material/Chat";
import CloseIcon from "@mui/icons-material/Close";
import ForwardToInboxIcon from "@mui/icons-material/ForwardToInbox";
import KeyboardBackspaceIcon from "@mui/icons-material/KeyboardBackspace";
import ListIcon from "@mui/icons-material/List";
import SearchIcon from "@mui/icons-material/Search";
import {
  Avatar,
  Box,
  Button,
  IconButton,
  Modal,
  TextField,
} from "@mui/material";
import axios from "axios";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { searchChatUser } from "../../Store/Auth/Action";
import "./Chat.css";

const Chat = React.memo(() => {
  const style = {
    position: "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    width: 600,
    minHeight: 600,
    maxHeight: 600,
    bgcolor: "background.paper",
    p: 2,
    borderRadius: 3,
    outline: "none",
    overflow: "scroll-y",
  };

  const style2 = {
    position: "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    width: 400,
    // minHeight: 200,
    // maxHeight: 200,
    bgcolor: "background.paper",
    boxShadow: 24,
    p: 2,
    borderRadius: 3,
    outline: "none",
    overflow: "scroll-y",
  };

  const chatStyle = {
    position: "absolute",
    top: "91%",
    right: "1%",
    width: 580,
  };

  const userStyle = {
    position: "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    width: 285,
    minHeight: 200,
    maxHeight: 200,
    bgcolor: "background.paper",
    p: 2,
    borderRadius: 3,
    outline: "none",
    overflow: "scroll-y",
  };

  const [roomName, setRoomName] = useState("");
  const [chatRooms, setChatRooms] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [selectedRoomId, setSelectedRoomId] = useState(null);

  const [chatHistory, setChatHistory] = useState([]);
  const [message, setMessage] = useState("");
  const [modalIsOpen, setModalIsOpen] = useState(false);

  const [stompClient, setStompClient] = useState(null);
  const [error, setError] = useState("");

  const { auth, theme } = useSelector((store) => store);

  const [search, setSearch] = useState("");
  const dispatch = useDispatch();

  const [completeCreated, setCompleteCreated] = useState(0);
  const [completeDeleted, setCompleteDeleted] = useState(0);
  const [completeEdited, setCompleteEdited] = useState(0);

  const [finduserrender, setFinduserrender] = useState(0);
  const [showUserList, setShowUserList] = useState(false);

  const [editModalIsOpen, setEditModalIsOpen] = useState(false);
  const [selectedEditRoomId, setSelectedEditRoomId] = useState("");
  const [newRoomName, setNewRoomName] = useState("");
  const navigate = useNavigate();
  const handleBack = () => {
    navigate(-1);
  };
  const [modalState, setModalState] = useState(false);

  const scrollRef = useRef();

  useEffect(() => {
    scrollToBottom();
  }, [message]);

  function OnOffModal() {
    if (modalState === true) {
      setModalState(false);
    } else {
      setModalState(true);
    }
  }

  const [userList, setUserList] = useState([]);

  // useEffect(() => {
  //   //웹소켓 연결
  //   const socket = new SockJS("http://localhost:8080/ws");
  //   const stompClient = Stomp.over(socket);
  //   const stoconnect = stompClient.connect({}, () => {
  //     setStompClient(stompClient);
  //   });

  //   console.log("stoconnect: ", stoconnect);
  // }, []);

  const createRoom = () => {
    if (!roomName) {
      setError("채팅방 명을 입력해주세요."); //채팅방 명을 안 넣을시 뜨는 함수
      return;
    }

    axios
      .post("http://localhost:8080/createroom", {
        name: roomName,
        creator: auth.user?.fullName,
        creatorEmail: auth.user?.email.split(" ")[0],
      })
      .then((response) => {
        if (response.status === 201) {
          //채팅방을 만드면 자동으로 렌더링 해주는 곳
          setChatRooms([...chatRooms, response.data]); //채팅방 내역
          setRoomName(""); //채팅방 이름 초기화
          setError(""); //에러 메시지 초기화
          setCompleteCreated((prev) => prev + 1); //부분 렌더링
        }
      })
      .catch((error) => {});
  };

  const handleSearchChatUser = (event) => {
    setSearch(event.target.value);
    dispatch(searchChatUser(event.target.value));
  };

  const enterChatRoom = (roomId, roomname) => {
    const chatWindow = document.getElementById("chat-window");
  
    if (chatWindow) {
      chatWindow.addEventListener("DOMNodeInserted", () => {
        chatWindow.scrollTop = chatWindow.scrollHeight;
      });
    }
  
    axios
      .post("http://localhost:8080/getchat", roomId, {
        headers: {
          "Content-Type": "text/plain",
        },
      })
      .then((response) => {
        if (response.status === 200) {
          setChatHistory(response.data); //해당 방 이전 채팅 내역을 출력
        }
      })
      .catch((error) => {});
  
    setSelectedRoom(roomname); // Set the selectedRoom to the room name
    setSelectedRoomId(roomId); //해당 채팅방 id
    setModalIsOpen(true); //모달 열어둠
  
    // Use navigate to the correct URL
    navigate(`/messages`); // Update the path as needed
  
    // Return the room details here
    return { roomId, roomName: roomname };
  };
  
  const openEditModal = (roomId, roomname) => {
    setSelectedEditRoomId(roomId); //해당 채팅방 id
    setNewRoomName(roomname); //기존 채팅방 이름이자, 새롭게 입력될 채팅방 이름
    setEditModalIsOpen(true); //모달창 오픈
  };

  const saveEditedRoomName = () => {
    axios
      .post("http://localhost:8080/editroom", {
        roomId: selectedEditRoomId, //기존 채팅방 id와 새롭게 작성한 채팅방 이름을 전송
        name: newRoomName,
      })
      .then((response) => {
        if (response.status === 200) {
          setEditModalIsOpen(false); //모달 닫음
          setCompleteEdited((prev) => prev + 1); //부분 렌더링
        }
      })
      .catch((error) => {});
  };

  useEffect(() => {
    const socket = new SockJS("http://localhost:8080/ws");
    const stompClient = Stomp.over(socket);

    const onMessageReceived = (message) => {
      const chatMessage = JSON.parse(message.body);
      console.log("chatMessage", chatMessage);

      if (chatMessage.roomId === selectedRoomId) {
        if (auth.user?.email.split(" ")[0] !== chatMessage.email) {
          const notificationMessage = `${chatMessage.sender}: ${chatMessage.message}`;

          toast.info(notificationMessage, {
            onClick: () =>
              handleNotificationClick(chatMessage.roomId, chatMessage.roomName),
          });
        }
      }
    };

    const handleNotificationClick = (roomId) => {
      enterChatRoom(roomId, chatRooms.find(room => room.roomId === roomId)?.name || "");
    };       

    const connectCallback = () => {
      setStompClient(stompClient);

      const subscription = stompClient.subscribe(
        `/topic/${selectedRoomId}`,
        onMessageReceived
      );

      return () => {
        subscription.unsubscribe();
      };
    };

    stompClient.connect({}, connectCallback);
  }, [selectedRoomId]);

  const sendMessage = () => {
    setTimeout(() => {
      scrollToBottom();
    }, 0);

    if (stompClient) {
      const chatMessage = {
        type: "TALK",
        roomId: selectedRoomId,
        sender: auth.user?.fullName,
        email: auth.user?.email.split(" ")[0],
        message: message,
      };

      stompClient.send(
        `/app/savechat/${selectedRoomId}`,
        {},
        JSON.stringify(chatMessage)
      );

      setMessage("");
    } else {
      console.error("WebSocket 연결이 설정되지 않았습니다.");
    }
  };

  const UserAddList = (roomid, useremail, sendername) => {
    const adduser = {
      roomId: roomid,
      email: useremail,
      sender: sendername,
    };

    axios
      .post("http://localhost:8080/addusers", adduser) //유저추가시 보내는 경로
      .then((response) => {
        if (response.status === 201) {
          setFinduserrender((prev) => prev + 1); //유저 내역 부분 랜더링
          setSearch(""); //검색창 초기화
        }
      })
      .catch((error) => {});
  };

  useEffect(() => {
    if (stompClient && selectedRoomId) {
      //채팅이 오는 것을 받는 함수(받을때)
      const subscription = stompClient.subscribe(
        `/topic/${selectedRoomId}`,
        (message) => {
          const chatMessage = JSON.parse(message.body);
          setChatHistory((prevChatHistory) => [
            ...prevChatHistory,
            chatMessage,
          ]);
        }
      );

      return () => {
        subscription.unsubscribe();
      };
    }
  }, [stompClient, selectedRoom]);

  const deleteUserInRoom = (roomId) => {
    axios
      .post("http://localhost:8080/deleteusers", {
        email: auth.user?.email.split(" ")[0],
        roomId: roomId,
      })
      .then((response) => {
        if (response.status === 200) {
          setCompleteDeleted((prev) => prev + 1);
        } else if (response.status === 404) {
        }
      })
      .catch((error) => {});
  };

  useEffect(() => {
    axios.get("http://localhost:8080/allrooms").then((response) => {
      const rooms = response.data;
      console.log("rooms: ", rooms);
      setChatRooms(rooms);

      rooms.forEach((room) => {
        checkUserInRoom(room.roomId);
      });
    }, []);
  }, [completeCreated, completeDeleted, completeEdited]); //부분 렌더링

  useEffect(() => {
    if (modalIsOpen) {
      axios
        .post("http://localhost:8080/findusers", selectedRoomId, {
          headers: {
            "Content-Type": "text/plain",
          },
        })
        .then((response) => {
          setUserList(response.data);
        })
        .catch((error) => {});
    }
  }, [modalIsOpen, selectedRoomId, finduserrender]); //부분 렌더링

  const checkUserInRoom = (roomId) => {
    axios
      .post("http://localhost:8080/finduser", {
        email: auth.user?.email.split(" ")[0],
        roomId: roomId,
      })
      .then((response) => {
        if (response.status === 200) {
          setChatRooms((prevChatRooms) =>
            prevChatRooms.map((room) =>
              room.roomId === roomId ? { ...room, isVisible: true } : room
            )
          );
        } else if (response.status === 404) {
          setChatRooms((prevChatRooms) =>
            prevChatRooms.map((room) =>
              room.roomId === roomId ? { ...room, isVisible: false } : room
            )
          );
        }
      })
      .catch((error) => {});
  };

  const closeChatModal = () => {
    setModalIsOpen(false);
  };
  const closeEditModal = () => {
    setEditModalIsOpen(false);
  };
  const closeUserModal = () => {
    setShowUserList(false);
  };
  const openUserModal = () => {
    setShowUserList(true);
  };

  const setChatContainerRef = useCallback((node) => {
    if (node) {
      chatContainerRef.current = node;
      scrollToBottom();
    }
  }, []);

  const scrollToBottom = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  };

  useEffect(() => {
    scrollToBottom();
  }, [chatHistory]); // Assuming chatHistory is the state that contains the chat messages

  useEffect(() => {
    scrollToBottom();
  }, [chatHistory.length]); // Assuming chatHistory is the state that contains the chat messages

  useEffect(() => {
    const chatContainer = document.getElementById("chat-container");
    if (chatContainer) {
      const lastChatMessageId = chatHistory[chatHistory.length - 1]?.id;
      if (lastChatMessageId) {
        const lastChatMessage = document.getElementById(
          `chat-message-${lastChatMessageId}`
        );
        if (lastChatMessage) {
          lastChatMessage.scrollIntoView({ behavior: "auto" });
        }
      }
    }
  }, [chatHistory]);

  const chatContainerRef = useRef(null);

  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop =
        chatContainerRef.current.scrollHeight;
    }
  }, [chatHistory]);

  return (
    <div>
      <section
        className={`z-50 flex items-center sticky top-0 bg-opacity-95`}
      >
        <div className="z-50 flex items-center sticky top-0 space-x-5">
          <KeyboardBackspaceIcon
            className="cursor-pointer"
            onClick={handleBack}
          />
          <h1 className="py-5 text-xl font-bold opacity-90 ml-5">메시지</h1>
        </div>
        <div className="absolute right-0 cursor-pointer">
          <input
            value={roomName}
            type="text"
            placeholder="생성할 채팅방의 이름"
            onChange={(e) => setRoomName(e.target.value)}
            className={`py-3 rounded-full outline-none text-gray-500 pl-12 ${
              theme.currentTheme === "light" ? "bg-stone-300" : "bg-[#151515]"
            }`}
          />
          <span className="absolute top-0 left-0 pl-3 pt-3">
            <SearchIcon className="text-gray-400" />
          </span>
          <AddIcon onClick={createRoom} className="ml-3 cursor-pointer" />
        </div>
      </section>
      {error && <div style={{ color: "red" }}>{error}</div>}
      <div className="space-y-3" style={{ marginTop: 20 }}>
        {chatRooms.length > 0 ? ( //채팅방 목록 출력
          chatRooms.map((room) => (
            <div
              className="chat-card"
              key={room.roomId}
              style={{
                padding: "10px",
                marginBottom: "10px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                display: room.isVisible ? "block" : "none",
              }}
            >
              <div
                className="cursor-pointer"
                style={{ width: "100%" }}
                onClick={() => enterChatRoom(room.roomId, room.name)}
              >
                <button
                  //채팅방 입장
                  style={{ cursor: "pointer", fontSize: "larger" }}
                >
                  <ChatIcon style={{ marginLeft: 10, marginRight: 10 }} />
                  <span style={{ marginLeft: "5px" }}>{room.name}</span>
                </button>
              </div>
              <div>
                <Button
                  onClick={() => openEditModal(room.roomId, room.name)} //채팅방 정보 수정
                  style={{
                    cursor: "pointer",
                    marginRight: "5px",
                    fontFamily: "ChosunGu",
                  }}
                >
                  이름 변경
                </Button>
                /
                <Button
                  onClick={() => deleteUserInRoom(room.roomId)} //채팅방 나가기
                  style={{ cursor: "pointer", fontFamily: "ChosunGu" }}
                >
                  나가기
                </Button>
              </div>
            </div>
          ))
        ) : (
          <div>채팅방이 없습니다</div>
        )}
      </div>
      <Modal open={modalIsOpen} onClose={closeChatModal}>
        <Box sx={style}>
          <section
            className={`z-50 flex items-center sticky top-0 bg-opacity-95`}
          ></section>

          <div>
            <div className="z-50 flex items-center sticky top-0 space-x-5">
              <IconButton onClick={closeChatModal} aria-label="delete">
                <CloseIcon />
              </IconButton>
              <h1 className="text-xl">{selectedRoom}</h1>
            </div>
            <input
              style={{ marginTop: 10 }}
              value={search}
              onChange={handleSearchChatUser} //채팅방에 초대할 유저 입력
              type="text"
              placeholder="유저 검색"
              className={`py-2 rounded-full outline-none text-gray-500 pl-12 ${
                theme.currentTheme === "light" ? "bg-stone-300" : "bg-[#080808]"
              }`}
            />
            <span className="absolute top-16 left-4 pl-3 pt-3">
              <SearchIcon className="text-gray-400" />
            </span>
            <IconButton
              onClick={openUserModal} // Toggle showUserList
              aria-label="list"
            >
              <ListIcon />
            </IconButton>
            {search && (
              <div>
                {auth.userSearchResult &&
                  auth.userSearchResult.map(
                    (item) =>
                      !userList.some((user) => user.email === item.email) && (
                        //초대할 유저 검색, rightpart에 있는 '사용자 및 글 검색'그대로 가져와 수정함
                        <div
                          key={item.id}
                          className={`rounded-full outline-none text-gray-500 pl-6 ${
                            theme.currentTheme === "light"
                              ? "hover:bg-[#008000]"
                              : "hover:bg-[#dbd9d9]"
                          }
                          ${
                            theme.currentTheme === "light"
                              ? "text-black hover:text-white"
                              : "text-white  hover:text-black"
                          } cursor-pointer`}
                          onClick={() => {
                            UserAddList(
                              selectedRoomId,
                              item.email.split(" ").join("_").toLowerCase(),
                              item.fullName
                            );
                          }}
                        >
                          <div className="flex items-center space-x-2">
                            <Avatar
                              alt={item.fullName}
                              src={
                                item.image
                                  ? item.image
                                  : "https://cdn.pixabay.com/photo/2023/10/24/01/42/art-8337199_1280.png"
                              }
                              loading="lazy"
                            ></Avatar>
                            <div>
                              <p>{item.fullName}</p>
                              <p>
                                {item.email.split(" ").join("_").toLowerCase()}
                              </p>
                            </div>
                          </div>
                        </div>
                      )
                  )}
              </div>
            )}
          </div>
          {showUserList && (
            <Modal open={showUserList} onClose={closeUserModal}>
              <Box sx={userStyle}>
                <div
                  className={`p-4 rounded-md customeScrollbar overflow-y-scroll css-scroll hideScrollbar h-[19vh] ${
                    theme.currentTheme === "light" ? "bg-white" : "bg-[#0D0D0D]"
                  }`}
                >
                  <div className="flex items-center justify-between mb-4">
                    <h3
                      className={`text-lg font-bold ${
                        theme.currentTheme === "light"
                          ? "text-black"
                          : "text-white"
                      }`}
                    >
                      참여 중인 멤버
                    </h3>
                    <button onClick={() => closeUserModal()}>
                      <CloseIcon
                        className={`${
                          theme.currentTheme === "light"
                            ? "text-black"
                            : "text-white"
                        }`}
                      />
                    </button>
                  </div>
                  <ul>
                    {userList.map((user) => (
                      <li key={user.email} className="flex items-center mb-2">
                        <Avatar
                          alt={user.fullName}
                          src={
                            user?.image
                              ? user.image
                              : "https://cdn.pixabay.com/photo/2023/10/24/01/42/art-8337199_1280.png"
                          }
                          loading="lazy"
                        />
                        <div
                          className={`${
                            theme.currentTheme === "light"
                              ? "text-black"
                              : "text-white"
                          }`}
                        >
                          <p className="font-bold">{user.fullName}</p>
                          <p className="opacity-70">
                            {user.email.split(" ")[0]}
                          </p>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              </Box>
            </Modal>
          )}
          <hr
            style={{
              marginTop: 10,
              marginBottom: 1,
              background: "grey",
              color: "grey",
              borderColor: "grey",
              height: "1px",
            }}
          />
          <div
            className={`customeScrollbar overflow-y-scroll css-scroll h-[45vh]`}
            ref={scrollRef}
          >
            {chatHistory.length > 0 ? ( //채팅 내역
              chatHistory.map((chat) => (
                <div
                  key={chat.id}
                  style={{
                    whiteSpace: "pre-line",
                    marginBottom: "10px",
                    display: "flex",
                    justifyContent:
                      auth.user?.fullName === chat.sender
                        ? "flex-end"
                        : "flex-start",
                  }}
                >
                  {chat.message && (
                    <span
                      id={`chat-message-${chat.id}`}
                      className="text-xl rounded-full outline-none text-gray-500"
                      style={{
                        display: "block",
                        padding: "10px", // Add padding to create space within the message box
                        textAlign:
                          auth.user?.fullName === chat.sender
                            ? "right"
                            : "left",
                        textColor:
                          auth.user?.fullName === chat.sender
                            ? "grey"
                            : "#07bc0c",
                      }}
                    >
                      {auth.user?.fullName === chat.sender ? (
                        <p
                          className={`${
                            theme.currentTheme === "light"
                              ? "bg-stone-200 text-black"
                              : "bg-stone-800 text-white"
                          }`}
                          style={{
                            padding: "10px",
                            borderRadius: "10px",
                            maxWidth: "100%",
                          }}
                        >
                          <div
                            ref={setChatContainerRef}
                            id="chat-container"
                            style={{
                              overflowY: "auto",
                              maxHeight: "400px",
                            }}
                          >
                            {chat.message}
                          </div>
                        </p>
                      ) : (
                        <div
                          style={{ display: "flex", alignItems: "flex-start" }}
                        >
                          <Avatar
                            onClick={() =>
                              navigate(`/profile/${chat.sender?.id}`)
                            }
                            alt="Avatar"
                            src={
                              chat.sender.image
                                ? chat.sender.image
                                : "https://cdn.pixabay.com/photo/2023/10/24/01/42/art-8337199_1280.png"
                            }
                            className="cursor-pointer"
                            loading="lazy"
                            style={{ marginRight: "10px" }}
                          />
                          <div>
                            {chat.sender && (
                              <p
                                style={{ margin: "-7px 0 0", fontSize: "14px" }}
                              >
                                {chat.sender}
                              </p>
                            )}
                            <p
                              className={`${
                                theme.currentTheme === "light"
                                  ? "bg-stone-200 text-black"
                                  : "bg-stone-800 text-white"
                              }`}
                              style={{
                                // backgroundColor:
                                //   auth.user?.fullName === chat.sender
                                //     ? "#a5d8d1"
                                //     : "#f2f2f2",
                                padding: "10px",
                                borderRadius: "10px",
                                maxWidth: "100%",
                              }}
                            >
                              <div
                                ref={setChatContainerRef}
                                id="chat-container"
                                style={{
                                  overflowY: "auto",
                                  maxHeight: "400px",
                                }}
                              >
                                {chat.message}
                              </div>
                            </p>
                          </div>
                        </div>
                      )}
                    </span>
                  )}
                </div>
              ))
            ) : (
              <div style={{ marginTop: 10 }}>아직 채팅 내역이 없습니다.</div>
            )}
          </div>
          <div style={chatStyle}>
            <div style={{ display: "flex", alignItems: "center" }}>
              <textarea
                type="text"
                placeholder="메시지 보내기..."
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    sendMessage();
                  }
                }}
                style={{
                  width: "100%",
                  height: "40px",
                  resize: "none",
                  boxSizing: "border-box",
                  marginRight: "10px", // 왼쪽 여백 추가
                }}
                className={`py-2 rounded-full outline-none text-gray-500 pl-12 ${
                  theme.currentTheme === "light"
                    ? "bg-stone-300"
                    : "bg-[#151515]"
                }`}
              />
              <ForwardToInboxIcon
                fontSize="large"
                onClick={() => {
                  sendMessage();
                  scrollToBottom(); // 메시지를 보낸 후에 화면을 맨 아래로 스크롤
                }}
                style={{ cursor: "pointer" }}
              />
            </div>
          </div>
        </Box>
      </Modal>
      <Modal open={editModalIsOpen} onClose={closeEditModal}>
        <Box sx={style2}>
          <TextField
            style={{ marginTop: 10 }}
            fullWidth
            multiline
            rows={1}
            id="chatroom name"
            name="chatroom name"
            label="채팅방 이름"
            value={newRoomName}
            onChange={(e) => setNewRoomName(e.target.value)}
          />
          <Button
            variant="outlined"
            style={{ marginTop: 20 }}
            onClick={saveEditedRoomName}
          >
            변경
          </Button>
        </Box>
      </Modal>

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
});

export default Chat;
