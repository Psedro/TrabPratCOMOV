const bcrypt = require("bcryptjs");
const express = require("express");
const cors = require("cors");
const { MongoClient, ObjectId } = require("mongodb");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");
require("dotenv").config();

const app = express();

app.use(cors());
app.use(express.json());

const uploadDir = path.join(__dirname, "uploads", "cvs");

fs.mkdirSync(uploadDir, { recursive: true });

app.use("/uploads", express.static(path.join(__dirname, "uploads")));

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    cb(null, `${crypto.randomUUID()}${ext}`);
  }
});

const upload = multer({
  storage,
  limits: {
    fileSize: 5 * 1024 * 1024
  },
  fileFilter: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const allowedExtensions = [".pdf", ".doc", ".docx"];

    if (!allowedExtensions.includes(ext)) {
      return cb(new Error("O currículo deve ser PDF, DOC ou DOCX."));
    }

    cb(null, true);
  }
});

function apagarFicheiro(caminho) {
  if (!caminho) return;

  fs.unlink(caminho, (error) => {
    if (error) {
      console.error("Erro ao apagar ficheiro:", error.message);
    }
  });
}
const PORT = process.env.PORT || 3000;
const MONGO_URI = process.env.MONGO_URI;
const DB_NAME = process.env.DB_NAME || "commov_db";

let db;

async function connectToMongo() {
  try {
    if (!MONGO_URI) {
      console.error("Erro: MONGO_URI não está definida no ficheiro .env");
      process.exit(1);
    }

    const client = new MongoClient(MONGO_URI);
    await client.connect();

    db = client.db(DB_NAME);

    console.log("Ligação ao MongoDB feita com sucesso.");
    console.log("Base de dados:", DB_NAME);
  } catch (error) {
    console.error("Erro ao ligar ao MongoDB:", error.message);
    process.exit(1);
  }
}

app.get("/", (req, res) => {
  res.json({
    message: "API COMMOV a funcionar",
    database: DB_NAME
  });
});

app.get("/health", async (req, res) => {
  try {
    await db.command({ ping: 1 });

    res.json({
      status: "OK",
      message: "Backend ligado ao MongoDB Atlas"
    });
  } catch (error) {
    res.status(500).json({
      status: "ERROR",
      message: "Erro na ligação ao MongoDB",
      error: error.message
    });
  }
});

app.get("/collections", async (req, res) => {
  try {
    const collections = await db.listCollections().toArray();
    res.json(collections.map(collection => collection.name));
  } catch (error) {
    res.status(500).json({
      message: "Erro ao listar coleções",
      error: error.message
    });
  }
});

app.get("/roles", async (req, res) => {
  try {
    const roles = await db.collection("roles").find().toArray();
    res.json(roles);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar roles",
      error: error.message
    });
  }
});

app.get("/users", async (req, res) => {
  try {
    const users = await db.collection("users").find().toArray();
    res.json(users);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar utilizadores",
      error: error.message
    });
  }
});

app.post("/users", async (req, res) => {
  try {
    const user = {
      firstName: req.body.firstName,
      lastName: req.body.lastName,
      email: req.body.email,
      passwordHash: req.body.passwordHash,
      status: req.body.status || "active",
      roleId: new ObjectId(req.body.roleId),
      createdAt: new Date(),
      updatedAt: new Date()
    };

    const result = await db.collection("users").insertOne(user);

    res.status(201).json({
      message: "Utilizador criado com sucesso",
      insertedId: result.insertedId
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao criar utilizador",
      error: error.message
    });
  }
});

app.get("/internship-offers", async (req, res) => {
  try {
    const offers = await db.collection("internshipOffers").find().toArray();
    res.json(offers);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar ofertas de estágio",
      error: error.message
    });
  }
});

app.post("/internship-offers", async (req, res) => {
  try {
    console.log("BODY OFERTA:", req.body);

    const {
      name,
      description,
      requirements,
      duration_in_months,
      total_spots,
      application_deadline,
      companyName,
      location
    } = req.body;

    if (
      !name ||
      !description ||
      !requirements ||
      !duration_in_months ||
      !total_spots ||
      !application_deadline ||
      !companyName ||
      !location
    ) {
      return res.status(400).json({
        message: "Campos obrigatórios em falta"
      });
    }

    const industry = await db.collection("industries").findOne({});
    const companyLocation = await db.collection("companyLocations").findOne({});

    if (!industry) {
      return res.status(400).json({
        message: "Não existe nenhuma indústria na coleção industries"
      });
    }

    if (!companyLocation) {
      return res.status(400).json({
        message: "Não existe nenhuma localização na coleção companyLocations"
      });
    }

    const offer = {
      name,
      description,
      requirements,

      durationInMonths: Number(duration_in_months),
      totalSpots: Number(total_spots),
      applicationDeadline: new Date(application_deadline),

      isActive: true,
      companyName,
      location,
      workModel: "Presencial",

      industryId: industry._id,
      companyLocationId: companyLocation._id,

      createdAt: new Date(),
      updatedAt: new Date()
    };

    console.log("OFERTA A INSERIR:", offer);

    const result = await db.collection("internshipOffers").insertOne(offer);

    console.log("OFERTA INSERIDA COM ID:", result.insertedId);

    res.status(201).json({
      message: "Oferta criada com sucesso",
      insertedId: result.insertedId.toString()
    });
  } catch (error) {
    console.error("ERRO AO CRIAR OFERTA:", error);
    console.error(
      "DETALHES VALIDAÇÃO:",
      JSON.stringify(error.errInfo?.details, null, 2)
    );

    res.status(500).json({
      message: "Erro ao criar oferta de estágio",
      error: error.message
    });
  }
});



app.post("/applications", upload.single("cv"), async (req, res) => {
  let documentInsertedId = null;

  try {
    console.log("====== RECEBI /applications ======");
    console.log("BODY:", req.body);
    console.log("FILE:", req.file);
    const {
      userId,
      internshipOfferId,
      availableFrom
    } = req.body;

    const responderErro = (status, message) => {
      console.log("ERRO CONTROLADO:", status, message);
      apagarFicheiro(req.file?.path);
      return res.status(status).json({ message });
    };

    if (!req.file) {
      return responderErro(400, "Currículo obrigatório.");
    }

    if (!userId) {
      return responderErro(400, "userId é obrigatório.");
    }

    if (!internshipOfferId) {
      return responderErro(400, "internshipOfferId é obrigatório.");
    }

    if (!ObjectId.isValid(userId)) {
      return responderErro(400, "userId inválido.");
    }

    if (!ObjectId.isValid(internshipOfferId)) {
      return responderErro(400, "internshipOfferId inválido.");
    }

    const userObjectId = new ObjectId(userId);
    const offerObjectId = new ObjectId(internshipOfferId);

    const user = await db.collection("users").findOne({
      _id: userObjectId
    });

    if (!user) {
      return responderErro(404, "Utilizador não encontrado.");
    }

    const role = await db.collection("roles").findOne({
      _id: user.roleId
    });

    if (role?.name !== "student") {
      return responderErro(403, "Apenas estudantes podem candidatar-se.");
    }

    const student = await db.collection("students").findOne({
      userId: userObjectId
    });

    if (!student) {
      return responderErro(404, "Estudante não encontrado.");
    }

    const offer = await db.collection("internshipOffers").findOne({
      _id: offerObjectId
    });

    if (!offer) {
      return responderErro(404, "Oferta de estágio não encontrada.");
    }

    const existingApplication = await db.collection("applications").findOne({
      studentId: student._id,
      internshipOfferId: offerObjectId
    });

    if (existingApplication) {
      return responderErro(409, "Já existe uma candidatura para esta oferta.");
    }

    const availableFromDate = availableFrom
      ? new Date(availableFrom)
      : new Date();

    if (isNaN(availableFromDate.getTime())) {
      return responderErro(400, "availableFrom inválido.");
    }

    const document = {
      fileName: req.file.originalname,
      filePath: `/uploads/cvs/${req.file.filename}`,
      fileSize: req.file.size,
      category: "cv",
      uploadedAt: new Date()
    };

    const documentResult = await db.collection("documents").insertOne(document);
    documentInsertedId = documentResult.insertedId;
    console.log("DOCUMENTO CRIADO:", documentInsertedId.toString());

    const application = {
      appliedDate: new Date(),
      status: "pending",
      coverLetter: "",
      availableFrom: availableFromDate,
      cvDocumentId: documentInsertedId,
      studentId: student._id,
      internshipOfferId: offerObjectId
    };

    const result = await db.collection("applications").insertOne(application);
    console.log("CANDIDATURA CRIADA:", result.insertedId.toString());

    res.status(201).json({
      message: "Candidatura submetida com sucesso.",
      application: {
        id: result.insertedId.toString(),
        status: application.status,
        cvDocumentId: documentInsertedId.toString()
      }
    });
  } catch (error) {
    console.error("ERRO AO CRIAR CANDIDATURA:", error);
    console.error("DETALHES:", JSON.stringify(error.errInfo?.details, null, 2));

    if (documentInsertedId) {
      await db.collection("documents").deleteOne({
        _id: documentInsertedId
      });
    }

    apagarFicheiro(req.file?.path);

    res.status(500).json({
      message: "Erro ao criar candidatura",
      error: error.message
    });
  }
});

app.get("/student-applications", async (req, res) => {
  try {
    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({
        message: "userId é obrigatório"
      });
    }

    if (!ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const student = await db.collection("students").findOne({
      userId: new ObjectId(userId)
    });

    if (!student) {
      return res.json([]);
    }

    const applications = await db.collection("applications").aggregate([
      {
        $match: {
          studentId: student._id
        }
      },
      {
        $lookup: {
          from: "internshipOffers",
          localField: "internshipOfferId",
          foreignField: "_id",
          as: "offer"
        }
      },
      {
        $unwind: "$offer"
      },
      {
        $lookup: {
          from: "documents",
          localField: "cvDocumentId",
          foreignField: "_id",
          as: "cvDocument"
        }
      },
      {
        $unwind: {
          path: "$cvDocument",
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $project: {
          _id: { $toString: "$_id" },
          status: 1,
          appliedDate: 1,
          cvName: {
            $ifNull: ["$cvDocument.fileName", "Sem currículo"]
          },
          cvPath: {
            $ifNull: ["$cvDocument.filePath", null]
          },
          offerTitle: "$offer.name",
          companyName: "$offer.companyName",
          offerDescription: "$offer.description",
          location: "$offer.location"
        }
      },
      {
        $sort: {
          appliedDate: -1
        }
      }
    ]).toArray();

    res.json(applications);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar candidaturas do aluno",
      error: error.message
    });
  }
});
function separarNomeCompleto(nome) {
  const partes = (nome || "").trim().split(/\s+/);

  const firstName = partes.shift() || "";
  const lastName = partes.join(" ");

  return {
    firstName,
    lastName
  };
}

function obterLabelRole(tipo) {
  switch (tipo) {
    case "student":
      return "Estudante";
    case "teacher":
      return "Professor";
    case "company":
      return "Empresa";
    default:
      return "Utilizador";
  }
}

app.post("/api/auth/register", async (req, res) => {
  try {
    const {
      nome,
      email,
      username,
      password,
      tipo,
      estudante,
      professor,
      empresa
    } = req.body;
    console.log("BODY REGISTER:", req.body);
    console.log("TIPO RECEBIDO:", tipo);
    console.log("DADOS ESTUDANTE:", estudante);
    console.log("DADOS ESTUDANTE:", professor);
    console.log("DADOS ESTUDANTE:", empresa);

    if (!nome || !email || !username || !password || !tipo) {
      return res.status(400).json({
        message: "Nome, email, username, password e tipo são obrigatórios"
      });
    }

    const tiposValidos = ["student", "teacher", "company"];

    if (!tiposValidos.includes(tipo)) {
      return res.status(400).json({
        message: "Tipo de utilizador inválido"
      });
    }

    if (tipo === "student") {
      if (!estudante || !estudante.numeroAluno || !estudante.curso || !estudante.ano) {
        return res.status(400).json({
          message: "Dados do estudante incompletos"
        });
      }
    }

    if (tipo === "teacher") {
      if (!professor || !professor.numeroProfessor || !professor.departamento) {
        return res.status(400).json({
          message: "Dados do professor incompletos"
        });
      }
    }

    if (tipo === "company") {
      if (!empresa || !empresa.nomeEmpresa) {
        return res.status(400).json({
          message: "Dados da empresa incompletos"
        });
      }
    }

    const emailNormalizado = email.trim().toLowerCase();
    const usernameNormalizado = username.trim();

    const existingUser = await db.collection("users").findOne({
      $or: [
        { email: emailNormalizado },
        { username: usernameNormalizado }
      ]
    });

    if (existingUser) {
      return res.status(409).json({
        message: "Email ou username já existe"
      });
    }

    let role = await db.collection("roles").findOne({
      name: tipo
    });

    if (!role) {
      const roleResult = await db.collection("roles").insertOne({
        name: tipo,
        description: obterLabelRole(tipo),
        createdAt: new Date(),
        updatedAt: new Date()
      });

      role = {
        _id: roleResult.insertedId,
        name: tipo,
        description: obterLabelRole(tipo)
      };
    }

    let firstName;
    let lastName;

    if (tipo === "company") {
      firstName = empresa.nomeEmpresa.trim();
      lastName = "";
    } else {
      const nomeSeparado = separarNomeCompleto(nome);
      firstName = nomeSeparado.firstName;
      lastName = nomeSeparado.lastName;
    }

    const user = {
      firstName,
      lastName,
      username: usernameNormalizado,
      email: emailNormalizado,
      passwordHash: await bcrypt.hash(password, 10),
      status: "active",
      roleId: role._id,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    const userResult = await db.collection("users").insertOne(user);
    const userId = userResult.insertedId;

    if (tipo === "student") {
      console.log("VOU INSERIR STUDENT");

      try {
        const studentData = {
          userId: userId,
          indexNumber: Number(estudante.numeroAluno),
          studyYear: Number(estudante.ano),
          degreeLevel: "Licenciatura",
          addressId: new ObjectId("6a1ba421a0d6e4ac5d8c263b"),
          mainCvId: null
        };

        console.log("DADOS A INSERIR EM STUDENTS:", studentData);

        const resultStudent = await db.collection("students").insertOne(studentData);

        console.log("STUDENT INSERIDO COM ID:", resultStudent.insertedId);
      } catch (err) {
        console.error("ERRO AO INSERIR STUDENT:", err);
      }
    }

    if (tipo === "teacher") {
      await db.collection("teachers").insertOne({
        userId: userId,
        teacherNumber: professor.numeroProfessor,
        department: professor.departamento
      });
    }

    if (tipo === "company") {
      await db.collection("companies").insertOne({
        ownerUserId: userId,
        name: empresa.nomeEmpresa,
        website: empresa.website || "",
        description: empresa.descricao || "",
        industryIds: []
      });
    }

    res.status(201).json({
      message: "Utilizador registado com sucesso",
      user: {
        id: userId.toString(),
        nome: tipo === "company" ? empresa.nomeEmpresa : nome,
        email: emailNormalizado,
        username: usernameNormalizado,
        tipo: tipo,
        roleId: role._id.toString()
      }
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao registar utilizador",
      error: error.message
    });
  }
});

app.post("/api/auth/login", async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({
        message: "Email e password são obrigatórios"
      });
    }

    const user = await db.collection("users").findOne({
      email: email
    });

    if (!user) {
      return res.status(401).json({
        message: "Email ou password inválidos"
      });
    }

    const passwordValida = await bcrypt.compare(password, user.passwordHash);

    if (!passwordValida) {
      return res.status(401).json({
        message: "Email ou password inválidos"
      });
    }

    const role = await db.collection("roles").findOne({
      _id: user.roleId
    });

    res.json({
      message: "Login efetuado com sucesso",
      user: {
        id: user._id.toString(),
        nome: `${user.firstName || ""} ${user.lastName || ""}`.trim(),
        email: user.email,
        username: user.username,
        roleId: user.roleId?.toString(),
        tipo: role?.name || null
      }
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao fazer login",
      error: error.message
    });
  }
});

connectToMongo().then(() => {
  app.listen(PORT, () => {
    console.log(`Servidor a correr em http://localhost:${PORT}`);
  });
});