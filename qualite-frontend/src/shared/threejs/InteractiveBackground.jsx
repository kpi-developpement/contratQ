"use client";

import { useEffect, useRef } from 'react';
import * as THREE from 'three';
import styles from '../styles/unified.module.css';

export default function InteractiveBackground() {
  const mountRef = useRef(null);

  useEffect(() => {
    const currentMount = mountRef.current;
    if (!currentMount) return;
    
    // 1. Scene Setup
    const scene = new THREE.Scene();
    // N7iydo l'fond lgris - Nkhliwh blanc safi b7al l'hlib
    scene.background = new THREE.Color('#ffffff'); 
    
    // N-tcher l'camera chwia llour bach l'particles ybanou f l'ecran kaml
    const camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
    camera.position.z = 18;

    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, powerPreference: "high-performance" });
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    
    // N-forciw l'canvas bach yakol l'ecran kaml bla hwamich
    renderer.domElement.style.position = 'absolute';
    renderer.domElement.style.top = '0';
    renderer.domElement.style.left = '0';
    renderer.domElement.style.width = '100vw';
    renderer.domElement.style.height = '100vh';
    renderer.domElement.style.outline = 'none';
    
    currentMount.appendChild(renderer.domElement);

    const tempPositions = [];
    const tempColors = [];
    const tempSizes = []; // Jdida: Sizes mkhtalfin l kol particule

    // Couleurs Premium: Royal Blue Kyntus & Gold 
    const colorRoyalBlue = new THREE.Color('#1D4ED8');
    const colorGold = new THREE.Color('#D4AF37');
    const colorLightBlue = new THREE.Color('#60A5FA'); // Zedtlo bleu fatih bach y3ti volume

    // ==========================================
    // ETAPE 1: Génération dyal l'ktba "kyntus" (HD)
    // ==========================================
    const canvas = document.createElement('canvas');
    canvas.width = 2048;
    canvas.height = 1024;
    const ctx = canvas.getContext('2d', { willReadFrequently: true });
    
    ctx.fillStyle = 'white';
    ctx.font = '900 400px "Arial Black", Impact, sans-serif'; // Kebert ktba chwia
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('kyntus', 1024, 512);

    const imgData = ctx.getImageData(0, 0, 2048, 1024).data;
    
    // Nktro l'particles dyal l'ktba bach tban HD
    for (let y = 0; y < 1024; y += 3) {
      for (let x = 0; x < 2048; x += 3) {
        const index = (y * 2048 + x) * 4;
        const alpha = imgData[index + 3];
        
        if (alpha > 128) {
          const pX = (x - 1024) * 0.015;
          const pY = -(y - 512) * 0.015;
          // Zedt profondeur mzyana bach l'ktba tban 3D (Hologram)
          const pZ = (Math.random() - 0.5) * 2; 
          
          tempPositions.push(pX, pY, pZ);
          
          const rand = Math.random();
          let color = colorRoyalBlue;
          if (rand > 0.85) color = colorGold;
          else if (rand > 0.6) color = colorLightBlue;
          
          tempColors.push(color.r, color.g, color.b);
          tempSizes.push((Math.random() * 0.04) + 0.01); // Sizes mkhtalfin
        }
      }
    }

    // ==========================================
    // ETAPE 2: Génération dyal les "Vagues/Arcs" 
    // (Bghinaha tmchi mn l'9nt l'9nt bach t3mer l'ecran)
    // ==========================================
    const arcCount = 8; // Ktrna l'arcs
    for (let i = 0; i < arcCount; i++) {
      const p = i / (arcCount - 1); 
      
      // Khlina l'arcs kbaaaar yfouto l'ecran
      const start = new THREE.Vector3(-15 + p * 5, -8, (Math.random() - 0.5) * 5);
      const cp1 = new THREE.Vector3(-8 + p * 4, 12 - p * 6, (Math.random() - 0.5) * 5);
      const cp2 = new THREE.Vector3(8 - p * 4, 12 - p * 6, (Math.random() - 0.5) * 5);
      const end = new THREE.Vector3(15 - p * 5, -8 + p * 5, (Math.random() - 0.5) * 5);

      const curve = new THREE.CubicBezierCurve3(start, cp1, cp2, end);
      
      const pointsOnCurve = 4000; 
      for (let j = 0; j < pointsOnCurve; j++) {
        const t = j / pointsOnCurve;
        const point = curve.getPoint(t);
        
        // Dispersion: Khelina chwya d particles mfrtkin (Dust effect)
        const thickness = 0.8;
        const offsetX = (Math.random() - 0.5) * thickness;
        const offsetY = (Math.random() - 0.5) * thickness;
        const offsetZ = (Math.random() - 0.5) * thickness * 2;

        tempPositions.push(point.x + offsetX, point.y + offsetY, point.z + offsetZ);
        
        const rand = Math.random();
        let color = colorRoyalBlue;
        if (rand > 0.9) color = colorGold;
        else if (rand > 0.7) color = colorLightBlue;
        
        tempColors.push(color.r, color.g, color.b);
        tempSizes.push((Math.random() * 0.03) + 0.01);
      }
    }

    // ==========================================
    // ETAPE 3: Ambient Dust (Particles 3chwaiyin f l'khalfya)
    // ==========================================
    for (let i = 0; i < 3000; i++) {
      tempPositions.push(
        (Math.random() - 0.5) * 40, // X: wase3
        (Math.random() - 0.5) * 30, // Y: wase3
        (Math.random() - 0.5) * 15 - 5 // Z: Lour
      );
      
      const rand = Math.random();
      const color = rand > 0.8 ? colorGold : colorLightBlue;
      tempColors.push(color.r, color.g, color.b);
      tempSizes.push(Math.random() * 0.02);
    }

    // ==========================================
    // ETAPE 4: Setup dyal l'Geometry
    // ==========================================
    const particlesCount = tempPositions.length / 3;
    const positions = new Float32Array(particlesCount * 3);
    const targetPositions = new Float32Array(particlesCount * 3);
    const velocities = new Float32Array(particlesCount * 3);
    const colors = new Float32Array(tempColors);
    const sizes = new Float32Array(tempSizes);

    for (let i = 0; i < particlesCount * 3; i+=3) {
      targetPositions[i] = tempPositions[i];
      targetPositions[i+1] = tempPositions[i+1];
      targetPositions[i+2] = tempPositions[i+2];

      // Initial scatter (bach ytjm3o f lbedya b animation zewina)
      positions[i] = tempPositions[i] + (Math.random() - 0.5) * 20;
      positions[i+1] = tempPositions[i+1] + (Math.random() - 0.5) * 20;
      positions[i+2] = tempPositions[i+2] + (Math.random() - 0.5) * 20;

      velocities[i] = 0; velocities[i+1] = 0; velocities[i+2] = 0;
    }

    const geometry = new THREE.BufferGeometry();
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
    geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1)); // Zidna custom size

    // Material jdid: Glow effect b texture d'es particles
    const material = new THREE.PointsMaterial({
      size: 1.0, // Base size
      vertexColors: true,
      transparent: true,
      opacity: 0.8,
      blending: THREE.NormalBlending,
      depthWrite: false, // Bach particles ybanou foug b3dyathom mzyan
    });

    // Custom shader bach n3tiw l'particles chkl "mdower" w "glowy" blast mrb3at
    material.onBeforeCompile = (shader) => {
      shader.vertexShader = shader.vertexShader.replace(
        'gl_PointSize = size;',
        'gl_PointSize = size * attributeSize * ( 300.0 / -mvPosition.z );'
      );
      shader.vertexShader = 'attribute float attributeSize;\n' + shader.vertexShader;
      
      shader.fragmentShader = shader.fragmentShader.replace(
        'gl_FragColor = vec4( diffuse, opacity );',
        `
        float dist = length(gl_PointCoord - vec2(0.5));
        if (dist > 0.5) discard;
        float alpha = (0.5 - dist) * 2.0 * opacity;
        gl_FragColor = vec4(diffuse, alpha);
        `
      );
    };

    const particlesMesh = new THREE.Points(geometry, material);
    scene.add(particlesMesh);

    // ==========================================
    // ETAPE 5: Mouse Interaction (Shatter Effect)
    // ==========================================
    const mouse = new THREE.Vector2(9999, 9999);
    const raycaster = new THREE.Raycaster();
    const plane = new THREE.Plane(new THREE.Vector3(0, 0, 1), 0);
    const mouse3D = new THREE.Vector3(9999, 9999, 0);

    const onMouseMove = (event) => {
      mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
      mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
      raycaster.setFromCamera(mouse, camera);
      raycaster.ray.intersectPlane(plane, mouse3D);
    };

    document.addEventListener('mousemove', onMouseMove);

    // ==========================================
    // ETAPE 6: Animation Loop (Smooth Fluid)
    // ==========================================
    const clock = new THREE.Clock();

    const animate = () => {
      requestAnimationFrame(animate);
      
      const time = clock.getElapsedTime();
      const posAttr = geometry.attributes.position;
      const posArray = posAttr.array;
      
      const mouseRadius = 3.5; // Kbbarna zoun dyal l'interaction 
      const repulsionStrength = 2.0; 
      const returnSpeed = 0.05; 
      const friction = 0.88;

      for (let i = 0; i < particlesCount; i++) {
        const i3 = i * 3;
        
        const cx = posArray[i3];
        const cy = posArray[i3+1];
        const cz = posArray[i3+2];

        // L'FIX HNA: Jbedna l'positions d'origine lwla
        const origX = targetPositions[i3];
        const origY = targetPositions[i3+1];
        const tz = targetPositions[i3+2]; // Z makaytbdelch bl wave fhad l'cas

        // Mouvement 3chwa2i (Floating wave effect) m-bassi 3la l'origine
        const waveX = Math.sin(time * 0.5 + origY) * 0.2;
        const waveY = Math.cos(time * 0.3 + origX) * 0.2;

        // 3ad darna affectation
        const tx = origX + waveX;
        const ty = origY + waveY;

        const dx = cx - mouse3D.x;
        const dy = cy - mouse3D.y;
        const dist = Math.sqrt(dx*dx + dy*dy);

        // Repulsion (Mli doz l'souris)
        if (dist < mouseRadius) {
          const force = (mouseRadius - dist) / mouseRadius;
          velocities[i3] += (dx / dist) * force * repulsionStrength;
          velocities[i3+1] += (dy / dist) * force * repulsionStrength;
          velocities[i3+2] += (Math.random() - 0.5) * force * repulsionStrength * 2;
        }

        // Return b chwia (Elasticity)
        velocities[i3] += (tx - cx) * returnSpeed;
        velocities[i3+1] += (ty - cy) * returnSpeed;
        velocities[i3+2] += (tz - cz) * returnSpeed;

        velocities[i3] *= friction;
        velocities[i3+1] *= friction;
        velocities[i3+2] *= friction;

        posArray[i3] += velocities[i3];
        posArray[i3+1] += velocities[i3+1];
        posArray[i3+2] += velocities[i3+2];
      }
      
      posAttr.needsUpdate = true;

      // Rotation mzyana b chwiya (b7al galaxi)
      particlesMesh.rotation.y = Math.sin(time * 0.2) * 0.05;
      particlesMesh.rotation.x = Math.cos(time * 0.15) * 0.02;

      renderer.render(scene, camera);
    };

    animate();

    const handleResize = () => {
      camera.aspect = window.innerWidth / window.innerHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(window.innerWidth, window.innerHeight);
    };
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      document.removeEventListener('mousemove', onMouseMove);
      if (currentMount) currentMount.removeChild(renderer.domElement);
      geometry.dispose();
      material.dispose();
      renderer.dispose();
    };
  }, []);

  return <div className={styles.threeBackground} ref={mountRef} />;
}